package org.shareat.feature.profile.ui.editprofile

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.UpdateCustomerProfileParams
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCase

@Stable
@KoinViewModel
class EditProfileViewModel(
    private val loadProfileSettingsUseCase: LoadProfileSettingsUseCase,
    private val updateCustomerProfileUseCase: UpdateCustomerProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.FullNameChanged -> edit { copy(fullName = action.value) }
            is EditProfileAction.DisplayNameChanged -> edit { copy(displayName = action.value) }
            is EditProfileAction.PhoneNumberChanged -> edit { copy(phoneNumber = action.value) }
            EditProfileAction.Save -> save()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = loadProfileSettingsUseCase()) {
                is RepositoryResult.Success -> when (val settings = result.value) {
                    ProfileSettings.Guest -> _uiState.value = EditProfileUiState(
                        isLoading = false,
                        error = EditProfileError.UNAUTHENTICATED,
                    )
                    is ProfileSettings.User -> _uiState.value = EditProfileUiState(
                        accountId = settings.account.id,
                        fullName = settings.profile.fullName,
                        displayName = settings.profile.displayName,
                        email = settings.account.loginEmail.value,
                        phoneNumber = settings.profile.phoneNumber.orEmpty(),
                        preferredLanguage = ProfileLanguage.fromCode(
                            settings.profile.preferredLanguage,
                        ),
                        isLoading = false,
                    )
                    is ProfileSettings.RestaurantOwner -> _uiState.value = EditProfileUiState(
                        isLoading = false,
                        error = EditProfileError.CUSTOMER_ONLY,
                    )
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.toEditProfileError())
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val accountId = state.accountId ?: return
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, saveSucceeded = false, error = null) }
        viewModelScope.launch {
            when (val result = updateCustomerProfileUseCase(
                UpdateCustomerProfileParams(
                    accountId = accountId,
                    fullName = state.fullName,
                    displayName = state.displayName,
                    phoneNumber = state.phoneNumber,
                    preferredLanguage = state.preferredLanguage.code,
                ),
            )) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(
                        fullName = result.value.fullName,
                        displayName = result.value.displayName,
                        phoneNumber = result.value.phoneNumber.orEmpty(),
                        preferredLanguage = ProfileLanguage.fromCode(
                            result.value.preferredLanguage,
                        ),
                        isSaving = false,
                        saveSucceeded = true,
                    )
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSucceeded = false,
                        error = result.error.toEditProfileError(),
                    )
                }
            }
        }
    }

    private fun edit(transform: EditProfileUiState.() -> EditProfileUiState) {
        _uiState.update { it.transform().copy(saveSucceeded = false, error = null) }
    }
}

private fun RepositoryError.toEditProfileError(): EditProfileError = when (this) {
    RepositoryError.InvalidCredentials -> EditProfileError.INVALID_CREDENTIALS
    RepositoryError.Offline -> EditProfileError.OFFLINE
    RepositoryError.Unauthenticated -> EditProfileError.UNAUTHENTICATED
    RepositoryError.Forbidden -> EditProfileError.FORBIDDEN
    is RepositoryError.Unavailable -> EditProfileError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.AlreadyExists -> EditProfileError.ALREADY_EXISTS
    is RepositoryError.NotFound -> EditProfileError.NOT_FOUND
    is RepositoryError.Conflict, is RepositoryError.Validation -> EditProfileError.UNKNOWN
}
