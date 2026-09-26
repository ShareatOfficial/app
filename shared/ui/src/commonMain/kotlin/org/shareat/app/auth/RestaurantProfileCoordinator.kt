package org.shareat.app.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

sealed interface RestaurantProfileGateState {
    data object Checking : RestaurantProfileGateState

    /**
     * The session may access the main application. A null [role] denotes a guest; a restaurant
     * role selects the correct landing destination. A newly registered restaurant can open
     * its owner home before creating its profile.
     */
    data class Allowed(
        val role: AccountRole? = null,
        val restaurant: Restaurant? = null,
    ) : RestaurantProfileGateState
    data class Failure(val error: RepositoryError) : RestaurantProfileGateState
}

/**
 * Observes the authenticated account and determines whether it may use the app.
 */
class RestaurantProfileCoordinator(
    private val sessions: SessionCoordinator,
    private val auth: AuthRepository,
    private val accounts: AccountRepository,
    private val restaurants: RestaurantRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow<RestaurantProfileGateState>(RestaurantProfileGateState.Checking)
    val state: StateFlow<RestaurantProfileGateState> = _state.asStateFlow()

    init {
        scope.launch {
            sessions.state.collectLatest { sessionState ->
                when (sessionState) {
                    AuthSessionState.Initializing -> _state.value = RestaurantProfileGateState.Checking
                    AuthSessionState.Unauthenticated -> _state.value = RestaurantProfileGateState.Allowed()
                    AuthSessionState.RefreshUnavailable -> _state.value =
                        RestaurantProfileGateState.Failure(RepositoryError.Offline)
                    is AuthSessionState.Authenticated -> checkProfile(sessionState)
                }
            }
        }
    }

    fun retry() {
        val authenticated = sessions.state.value as? AuthSessionState.Authenticated ?: return
        scope.launch { checkProfile(authenticated) }
    }

    suspend fun signOut(): RepositoryResult<Unit> = auth.signOut()

    private suspend fun checkProfile(authenticated: AuthSessionState.Authenticated) {
        _state.value = RestaurantProfileGateState.Checking
        val account = when (val result = accounts.getAccount(authenticated.session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return fail(result.error)
        }
        if (account.role == AccountRole.Customer) {
            _state.value = RestaurantProfileGateState.Allowed(role = AccountRole.Customer)
            return
        }
        if (account.status != AccountStatus.Active) {
            fail(RepositoryError.Forbidden)
            return
        }
        when (val result = restaurants.getRestaurantForOwner(account.id)) {
            is RepositoryResult.Success -> _state.value = RestaurantProfileGateState.Allowed(
                role = AccountRole.Restaurant,
                restaurant = result.value,
            )
            is RepositoryResult.Failure -> when (result.error) {
                is RepositoryError.NotFound -> _state.value = RestaurantProfileGateState.Allowed(
                    role = AccountRole.Restaurant,
                )
                else -> _state.value = RestaurantProfileGateState.Failure(result.error)
            }
        }
    }

    private fun fail(error: RepositoryError) {
        _state.value = RestaurantProfileGateState.Failure(error)
    }
}
