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
            is EditProfileAction.PreferredLanguageChanged -> edit {
                copy(preferredLanguage = action.value)
            }
            EditProfileAction.ChangePhoto -> _uiState.update {
                it.copy(errorMessage = "Profile photo editing is not available yet.")
            }
            EditProfileAction.Save -> save()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = loadProfileSettingsUseCase()) {
                is RepositoryResult.Success -> when (val settings = result.value) {
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
                        errorMessage = "Personal profile editing is only available for customer accounts.",
                    )
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toEditProfileMessage())
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val accountId = state.accountId ?: return
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, saveSucceeded = false, errorMessage = null) }
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
                        errorMessage = result.error.toEditProfileMessage(),
                    )
                }
            }
        }
    }

    private fun edit(transform: EditProfileUiState.() -> EditProfileUiState) {
        _uiState.update { it.transform().copy(saveSucceeded = false, errorMessage = null) }
    }
}

private fun RepositoryError.toEditProfileMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "Your session credentials are no longer valid."
    RepositoryError.Offline -> "You appear to be offline. Try again when connected."
    RepositoryError.Unauthenticated -> "Your session has expired. Please sign in again."
    RepositoryError.Forbidden -> "This account is not allowed to update the profile."
    is RepositoryError.Unavailable -> "The service is temporarily unavailable."
    is RepositoryError.AlreadyExists -> "The ${entity} already exists."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "The requested ${entity} could not be found."
    is RepositoryError.Validation -> reason
}
