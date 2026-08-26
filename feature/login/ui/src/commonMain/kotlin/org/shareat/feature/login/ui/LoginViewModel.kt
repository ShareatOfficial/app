package org.shareat.feature.login.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
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
    val step: LoginStep = LoginStep.Welcome
)

@Stable
@KoinViewModel
class LoginViewModel(
    private val authRepository: AuthRepository,
//    private val registerUseCase: Unit,
//    private val signInUseCase: Unit,
//    private val requestPasswordResetUseCase: Unit,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailFieldChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordFieldChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onDisplayNameFieldChange(value: String) =
        _uiState.update { it.copy(displayName = value, errorMessage = null) }

    fun onSelectRole(value: AccountRole) = _uiState.update { it.copy(registrationRole = value) }
    fun setRegistration(value: Boolean) = _uiState.update {
        it.copy(isRegistration = value, errorMessage = null, recoverySent = false)
    }

    fun goTo(target: LoginStep) {
        setRegistration(target == LoginStep.Register)
        _uiState.value = _uiState.value.copy(step = target)
    }

    fun onLoginClick() {
        val snapshot = uiState.value
        val email = runCatching { EmailAddress(snapshot.email.trim()) }.getOrNull()
        if (email == null || snapshot.password.length < 8) {
            _uiState.update {
                it.copy(errorMessage = "Enter a valid email and a password of at least 8 characters.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    recoverySent = false
                )
            }
            val result = if (uiState.value.isRegistration) {
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
            _uiState.update {
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

    fun onRequestPasswordRecovery() {
        val email = runCatching { EmailAddress(uiState.value.email.trim()) }.getOrNull()
        if (email == null) {
            _uiState.update { it.copy(errorMessage = "Enter your email first.") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    recoverySent = false
                )
            }
            when (val result = authRepository.requestPasswordReset(email)) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(isLoading = false, recoverySent = true)
                }

                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toUserMessage())
                }
            }
        }
    }
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
