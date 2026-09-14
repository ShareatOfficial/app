package org.shareat.feature.lastactivity.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.lastactivity.domain.GetLastActivityUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LastActivityViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setup() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun guestAndRefreshUnavailableShowLoginCta() = runTest(dispatcher) {
        val sessions = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
        val vm = LastActivityViewModel(auth(sessions), GetLastActivityUseCase { RepositoryResult.Success(emptyList()) })
        advanceUntilIdle(); assertIs<LastActivityUiState.Guest>(vm.uiState.value)
        sessions.value = AuthSessionState.RefreshUnavailable; advanceUntilIdle(); assertIs<LastActivityUiState.Guest>(vm.uiState.value)
    }

    @Test fun authenticatedLoadsEmptyAndLogoutCannotLeakContent() = runTest(dispatcher) {
        val sessions = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
        val vm = LastActivityViewModel(auth(sessions), GetLastActivityUseCase { RepositoryResult.Success(emptyList()) })
        sessions.value = AuthSessionState.Authenticated(org.shareat.app.domain.model.AuthSession(AccountId("a"), org.shareat.app.domain.model.EmailAddress("a@b.com")))
        advanceUntilIdle(); assertIs<LastActivityUiState.Empty>(vm.uiState.value)
        sessions.value = AuthSessionState.Unauthenticated; advanceUntilIdle(); assertIs<LastActivityUiState.Guest>(vm.uiState.value)
    }


    @Test fun becomingVisibleAgainReloadsAuthenticatedActivity() = runTest(dispatcher) {
        val sessions = MutableStateFlow<AuthSessionState>(
            AuthSessionState.Authenticated(
                org.shareat.app.domain.model.AuthSession(
                    AccountId("a"),
                    org.shareat.app.domain.model.EmailAddress("a@b.com"),
                ),
            ),
        )
        var loads = 0
        val vm = LastActivityViewModel(
            auth(sessions),
            GetLastActivityUseCase {
                loads += 1
                RepositoryResult.Success(emptyList())
            },
        )
        advanceUntilIdle()

        vm.onScreenVisible()
        vm.onScreenVisible()
        advanceUntilIdle()

        assertEquals(2, loads)
    }
}

private fun auth(states: MutableStateFlow<AuthSessionState>) = object : AuthRepository {
    override fun observeSession() = states
    override suspend fun currentSession() = RepositoryResult.Success(null)
    override suspend fun register(credentials: org.shareat.app.domain.model.RegistrationCredentials) = error("unused")
    override suspend fun signIn(email: org.shareat.app.domain.model.EmailAddress, password: String) = error("unused")
    override suspend fun signOut() = error("unused")
    override suspend fun requestPasswordReset(email: org.shareat.app.domain.model.EmailAddress) = error("unused")
    override suspend fun updatePassword(password: String) = error("unused")
}
