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
import org.shareat.feature.login.ui.model.LoginError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val registrationRole: AccountRole = AccountRole.Customer,
    val isRegistration: Boolean = false,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
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
        _uiState.update { it.copy(email = value, error = null) }

    fun onPasswordFieldChange(value: String) =
        _uiState.update { it.copy(password = value, error = null) }

    fun onDisplayNameFieldChange(value: String) =
        _uiState.update { it.copy(displayName = value, error = null) }

    fun onSelectRole(value: AccountRole) = _uiState.update { it.copy(registrationRole = value) }
    fun setRegistration(value: Boolean) = _uiState.update {
        it.copy(isRegistration = value, error = null, recoverySent = false)
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
                it.copy(error = LoginError.INVALID_INPUT)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
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
                    is RepositoryResult.Success -> it.copy(
                        isLoading = false,
                        authenticated = true,
                    )
                    is RepositoryResult.Failure -> it.copy(
                        isLoading = false,
                        error = result.error.toLoginError(),
                    )
                }
            }
        }
    }

    fun onRequestPasswordRecovery() {
        val email = runCatching { EmailAddress(uiState.value.email.trim()) }.getOrNull()
        if (email == null) {
            _uiState.update { it.copy(error = LoginError.EMAIL_REQUIRED) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    recoverySent = false
                )
            }
            when (val result = authRepository.requestPasswordReset(email)) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(isLoading = false, recoverySent = true)
                }

                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.toLoginError())
                }
            }
        }
    }
}

private fun RepositoryError.toLoginError(): LoginError = when (this) {
    RepositoryError.InvalidCredentials -> LoginError.INVALID_CREDENTIALS
    RepositoryError.Offline -> LoginError.OFFLINE
    RepositoryError.Unauthenticated -> LoginError.UNAUTHENTICATED
    RepositoryError.Forbidden -> LoginError.FORBIDDEN
    is RepositoryError.Unavailable -> LoginError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.AlreadyExists -> LoginError.ALREADY_EXISTS
    is RepositoryError.NotFound -> LoginError.NOT_FOUND
    is RepositoryError.Conflict, is RepositoryError.Validation -> LoginError.UNKNOWN
}
