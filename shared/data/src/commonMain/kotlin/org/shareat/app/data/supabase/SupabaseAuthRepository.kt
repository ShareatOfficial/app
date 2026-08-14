package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryResult

internal class SupabaseAuthRepository(
    private val client: SupabaseClient,
) : AuthRepository {
    override fun observeSession(): Flow<AuthSessionState> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.toDomainSession()
                ?.let(AuthSessionState::Authenticated)
                ?: AuthSessionState.Unauthenticated
            SessionStatus.Initializing -> AuthSessionState.Initializing
            is SessionStatus.RefreshFailure -> AuthSessionState.RefreshUnavailable
            is SessionStatus.NotAuthenticated -> AuthSessionState.Unauthenticated
        }
    }

    override suspend fun currentSession(): RepositoryResult<AuthSession?> = supabaseResult {
        client.auth.currentUserOrNull()?.toDomainSession()
    }

    override suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession> = supabaseResult {
        client.auth.signUpWith(Email) {
            email = credentials.email.value
            password = credentials.password
            data = buildJsonObject {
                put("account_role", credentials.role.toDatabaseValue())
                credentials.displayName?.let { put("display_name", it) }
            }
        }
        requireNotNull(client.auth.currentUserOrNull()?.toDomainSession()) {
            "Registration succeeded but did not establish a session"
        }
    }

    override suspend fun signIn(email: EmailAddress, password: String): RepositoryResult<AuthSession> = supabaseResult {
        client.auth.signInWith(Email) {
            this.email = email.value
            this.password = password
        }
        requireNotNull(client.auth.currentUserOrNull()?.toDomainSession()) {
            "Sign-in succeeded but did not establish a session"
        }
    }

    override suspend fun signOut(): RepositoryResult<Unit> = supabaseResult {
        client.auth.signOut()
    }

    override suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit> = supabaseResult {
        client.auth.resetPasswordForEmail(email.value)
    }

    override suspend fun updatePassword(password: String): RepositoryResult<Unit> = supabaseResult {
        require(password.length >= 8) { "Password must contain at least 8 characters" }
        client.auth.updateUser { this.password = password }
    }
}

private fun io.github.jan.supabase.auth.user.UserInfo.toDomainSession(): AuthSession = AuthSession(
    accountId = AccountId(id),
    email = EmailAddress(requireNotNull(email)),
)

private fun AccountRole.toDatabaseValue(): String = when (this) {
    AccountRole.Customer -> "customer"
    AccountRole.Restaurant -> "restaurant"
}
