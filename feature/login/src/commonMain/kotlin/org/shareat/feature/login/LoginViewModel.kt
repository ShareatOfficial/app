package org.shareat.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val registrationRole: AccountRole = AccountRole.Customer,
    val isRegistration: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recoverySent: Boolean = false,
    val authenticated: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = mutableState.asStateFlow()

    fun updateEmail(value: String) = mutableState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = mutableState.update { it.copy(password = value, errorMessage = null) }
    fun updateDisplayName(value: String) = mutableState.update { it.copy(displayName = value, errorMessage = null) }
    fun selectRole(value: AccountRole) = mutableState.update { it.copy(registrationRole = value) }
    fun setRegistration(value: Boolean) = mutableState.update {
        it.copy(isRegistration = value, errorMessage = null, recoverySent = false)
    }

    fun submit() {
        val snapshot = state.value
        val email = runCatching { EmailAddress(snapshot.email.trim()) }.getOrNull()
        if (email == null || snapshot.password.length < 8) {
            mutableState.update {
                it.copy(errorMessage = "Enter a valid email and a password of at least 8 characters.")
            }
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null, recoverySent = false) }
            val result = if (snapshot.isRegistration) {
                authRepository.register(
                    RegistrationCredentials(
                        email = email,
                        password = snapshot.password,
                        role = snapshot.registrationRole,
                        displayName = snapshot.displayName.trim().ifBlank { null },
                    ),
                )
            } else {
                authRepository.signIn(email, snapshot.password)
            }
            mutableState.update {
                when (result) {
                    is RepositoryResult.Success -> it.copy(isLoading = false, authenticated = true)
                    is RepositoryResult.Failure -> it.copy(
                        isLoading = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    fun requestPasswordRecovery() {
        val email = runCatching { EmailAddress(state.value.email.trim()) }.getOrNull()
        if (email == null) {
            mutableState.update { it.copy(errorMessage = "Enter your email first.") }
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null, recoverySent = false) }
            when (val result = authRepository.requestPasswordReset(email)) {
                is RepositoryResult.Success -> mutableState.update {
                    it.copy(isLoading = false, recoverySent = true)
                }
                is RepositoryResult.Failure -> mutableState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun consumeAuthentication() = mutableState.update { it.copy(authenticated = false) }
}

private fun RepositoryError.toUserMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "The email or password is incorrect."
    RepositoryError.Offline -> "You appear to be offline. Try again when connected."
    RepositoryError.Unauthenticated -> "Your session has expired. Please sign in again."
    RepositoryError.Forbidden -> "This account is not allowed to perform that action."
    RepositoryError.Unavailable -> "The service is temporarily unavailable."
    is RepositoryError.AlreadyExists -> "An account with that email already exists."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "The requested ${entity} could not be found."
    is RepositoryError.Validation -> reason
}
