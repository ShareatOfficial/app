package org.shareat.feature.login.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun restaurantRegistrationCompletesAuthenticationWithoutASeparateNavigationState() = runTest(dispatcher) {
        var receivedCredentials: RegistrationCredentials? = null
        val viewModel = LoginViewModel(
            authRepository = object : AuthRepository by unavailableAuthRepository() {
                override suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession> {
                    receivedCredentials = credentials
                    return RepositoryResult.Success(AuthSession(accountId = AccountId("owner"), email = credentials.email))
                }
            },
        )
        viewModel.goTo(LoginStep.Register)
        viewModel.onEmailFieldChange("owner@example.test")
        viewModel.onPasswordFieldChange("password1")
        viewModel.onSelectRole(AccountRole.Restaurant)

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals(AccountRole.Restaurant, receivedCredentials?.role)
        assertTrue(viewModel.uiState.value.authenticated)
    }
}

private fun unavailableAuthRepository(): AuthRepository = object : AuthRepository {
    override fun observeSession(): Flow<AuthSessionState> = emptyFlow()
    override suspend fun currentSession(): RepositoryResult<AuthSession> = unavailable()
    override suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession> = unavailable()
    override suspend fun signIn(email: EmailAddress, password: String): RepositoryResult<AuthSession> = unavailable()
    override suspend fun signOut(): RepositoryResult<Unit> = unavailable()
    override suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit> = unavailable()
    override suspend fun updatePassword(password: String): RepositoryResult<Unit> = unavailable()
}

private fun <T> unavailable(): RepositoryResult<T> =
    RepositoryResult.Failure(RepositoryError.Unavailable())
