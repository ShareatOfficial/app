package org.shareat.app.data.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeAuthRepository(
    initiallyAuthenticated: Boolean = false,
) : AuthRepository {
    private val session = AuthSession(FakeIds.customerAccount, EmailAddress("ana@example.com"))
    private val state = MutableStateFlow<AuthSessionState>(
        if (initiallyAuthenticated) AuthSessionState.Authenticated(session)
        else AuthSessionState.Unauthenticated,
    )

    override fun observeSession(): Flow<AuthSessionState> = state

    override suspend fun currentSession(): RepositoryResult<AuthSession?> = RepositoryResult.Success(
        (state.value as? AuthSessionState.Authenticated)?.session,
    )

    override suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession> =
        authenticate(credentials.email)

    override suspend fun signIn(email: EmailAddress, password: String): RepositoryResult<AuthSession> =
        if (password.length < 8) RepositoryResult.Failure(RepositoryError.InvalidCredentials)
        else authenticate(email)

    override suspend fun signOut(): RepositoryResult<Unit> {
        state.value = AuthSessionState.Unauthenticated
        return RepositoryResult.Success(Unit)
    }

    override suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit> =
        RepositoryResult.Success(Unit)

    override suspend fun updatePassword(password: String): RepositoryResult<Unit> =
        if (password.length < 8) {
            RepositoryResult.Failure(RepositoryError.Validation("Password must contain at least 8 characters"))
        } else {
            RepositoryResult.Success(Unit)
        }

    private fun authenticate(email: EmailAddress): RepositoryResult<AuthSession> {
        val authenticated = AuthSession(FakeIds.customerAccount, email)
        state.value = AuthSessionState.Authenticated(authenticated)
        return RepositoryResult.Success(authenticated)
    }
}
