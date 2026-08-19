package org.shareat.app.domain.model

data class AuthSession(
    val accountId: AccountId,
    val email: EmailAddress,
)

sealed interface AuthSessionState {
    data object Initializing : AuthSessionState
    data object Unauthenticated : AuthSessionState
    data class Authenticated(val session: AuthSession) : AuthSessionState
    data object RefreshUnavailable : AuthSessionState
}

data class RegistrationCredentials(
    val email: EmailAddress,
    val password: String,
    val role: AccountRole,
    val displayName: String? = null,
) {
    init {
        require(password.length >= 8)
        require(displayName == null || displayName.isNotBlank())
    }
}
