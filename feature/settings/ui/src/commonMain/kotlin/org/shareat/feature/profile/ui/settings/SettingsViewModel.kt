package org.shareat.feature.profile.ui.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.GetAppLanguageSupportUseCase
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.ObserveAppLanguageUseCase
import org.shareat.feature.profile.domain.SelectAppLanguageUseCase
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.SignOutUseCase
import org.shareat.feature.profile.domain.RequestAccountDeletionUseCase
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase

@Stable
@KoinViewModel
class SettingsViewModel(
    private val loadProfileSettingsUseCase: LoadProfileSettingsUseCase,
    private val updateRestaurantInfoUseCase: UpdateRestaurantInfoUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val observeAppLanguageUseCase: ObserveAppLanguageUseCase,
    private val getAppLanguageSupportUseCase: GetAppLanguageSupportUseCase,
    private val selectAppLanguageUseCase: SelectAppLanguageUseCase,
    private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
) : ViewModel() {
    private val eventChannel = Channel<SettingsEvent>(capacity = Channel.BUFFERED)
    internal val events: Flow<SettingsEvent> = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow<SettingsUiState>(
        SettingsUiState.User(isLoading = true),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var loadedRestaurant: Restaurant? = null
    private var deletionRequestInProgress = false

    /** Kept aside so replacing the whole state after a load or save does not drop the language. */
    private var languageState = AppLanguageUiState()

    init {
        loadSettings()
        observeLanguage()
    }

    fun onLanguageAction(action: SettingsLanguageAction) {
        viewModelScope.launch { selectAppLanguageUseCase(action.language) }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            val support = getAppLanguageSupportUseCase()
            observeAppLanguageUseCase().collect { language ->
                languageState = AppLanguageUiState(selected = language, support = support)
                updateCurrentState { withLanguage(languageState) }
            }
        }
    }

    fun onUserAction(action: SettingsUserAction) {
        when (action) {
            SettingsUserAction.EditProfile -> emitEvent(SettingsEvent.NavigateToEditProfile)
            SettingsUserAction.RequestDeletion -> requestAccountDeletion()
            SettingsUserAction.LogOut -> onLogOut()
        }
    }

    fun onRestaurantAction(action: SettingsRestaurantAction) {
        when (action) {
            is SettingsRestaurantAction.NameChanged -> changeRestaurantName(action.value)
            is SettingsRestaurantAction.DescriptionChanged -> changeRestaurantDescription(action.value)
            is SettingsRestaurantAction.PhoneChanged -> changeRestaurantPhone(action.value)
            is SettingsRestaurantAction.EmailChanged -> changeRestaurantEmail(action.value)
            is SettingsRestaurantAction.StreetChanged -> changeRestaurantStreet(action.value)
            is SettingsRestaurantAction.CityChanged -> changeRestaurantCity(action.value)
            is SettingsRestaurantAction.PostcodeChanged -> changeRestaurantPostcode(action.value)
            is SettingsRestaurantAction.VisibilityChanged -> changeRestaurantVisibility(action.value)
            is SettingsRestaurantAction.OpeningDayChanged ->
                changeOpeningDay(action.day, action.isOpen)

            is SettingsRestaurantAction.OpeningTimeChanged ->
                changeOpeningTime(action.day, action.value)

            is SettingsRestaurantAction.ClosingTimeChanged ->
                changeClosingTime(action.day, action.value)

            is SettingsRestaurantAction.OpeningHoursChanged ->
                editRestaurant { copy(openingHours = action.value) }

            SettingsRestaurantAction.Subscription -> emitEvent(SettingsEvent.NavigateToSubscription)
            SettingsRestaurantAction.RequestDeletion -> requestAccountDeletion()
            SettingsRestaurantAction.SaveChanges -> saveRestaurantChanges()
            SettingsRestaurantAction.LogOut -> onLogOut()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            when (val result = loadProfileSettingsUseCase()) {
                is RepositoryResult.Success -> when (val settings = result.value) {
                    is ProfileSettings.User ->
                        _uiState.value = settings.toUiState().withLanguage(languageState)
                    is ProfileSettings.RestaurantOwner -> {
                        loadedRestaurant = settings.restaurant
                        _uiState.value = settings.toUiState().withLanguage(languageState)
                    }
                }

                is RepositoryResult.Failure -> updateCurrentState {
                    when (this) {
                        is SettingsUiState.User -> copy(
                            isLoading = false,
                            error = result.error.toSettingsError(),
                        )

                        is SettingsUiState.Restaurant -> copy(
                            isLoading = false,
                            error = result.error.toSettingsError(),
                        )
                    }
                }
            }
        }
    }

    private fun changeRestaurantName(value: String) = editRestaurant { copy(name = value) }
    private fun changeRestaurantDescription(value: String) =
        editRestaurant { copy(description = value) }

    private fun changeRestaurantPhone(value: String) = editRestaurant { copy(phone = value) }
    private fun changeRestaurantEmail(value: String) = editRestaurant { copy(email = value) }
    private fun changeRestaurantStreet(value: String) =
        editRestaurant { copy(streetAddress = value) }

    private fun changeRestaurantCity(value: String) = editRestaurant { copy(city = value) }
    private fun changeRestaurantPostcode(value: String) = editRestaurant { copy(postcode = value) }
    private fun changeRestaurantVisibility(value: Boolean) =
        editRestaurant { copy(isPublished = value) }

    private fun changeOpeningDay(day: OpeningDay, isOpen: Boolean) = updateOpeningHours(day) {
        copy(isOpen = isOpen)
    }

    private fun changeOpeningTime(day: OpeningDay, value: String) = updateOpeningHours(day) {
        copy(openingTime = value)
    }

    private fun changeClosingTime(day: OpeningDay, value: String) = updateOpeningHours(day) {
        copy(closingTime = value)
    }

    private fun saveRestaurantChanges() {
        val state = _uiState.value as? SettingsUiState.Restaurant ?: return
        val original = loadedRestaurant ?: return updateRestaurant {
            copy(error = SettingsError.RestaurantUnavailable)
        }
        when (val mapping = state.toUpdateParams(original)) {
            is RestaurantSettingsMappingResult.Failure -> updateRestaurant {
                copy(error = mapping.error, saveSucceeded = false)
            }

            is RestaurantSettingsMappingResult.Success -> viewModelScope.launch {
                updateRestaurant {
                    copy(
                        isSaving = true,
                        error = null,
                        saveSucceeded = false
                    )
                }
                when (val result = updateRestaurantInfoUseCase(mapping.params)) {
                    is RepositoryResult.Success -> {
                        loadedRestaurant = result.value
                        _uiState.value = result.value.toUiState()
                            .copy(saveSucceeded = true, language = languageState)
                    }

                    is RepositoryResult.Failure -> updateRestaurant {
                        copy(
                            isSaving = false,
                            error = result.error.toSettingsError(),
                            saveSucceeded = false,
                        )
                    }
                }
            }
        }
    }

    private fun onLogOut() {
        if (_uiState.value.isLoading) return

        updateCurrentState {
            when (this) {
                is SettingsUiState.User -> copy(isLoading = true, error = null)
                is SettingsUiState.Restaurant -> copy(isLoading = true, error = null)
            }
        }
        viewModelScope.launch {
            when (val result = signOutUseCase()) {
                is RepositoryResult.Success -> {
                    updateCurrentState {
                        when (this) {
                            is SettingsUiState.User -> copy(isLoading = false)
                            is SettingsUiState.Restaurant -> copy(isLoading = false)
                        }
                    }
                    eventChannel.send(SettingsEvent.LogoutSuccess)
                }

                is RepositoryResult.Failure -> updateCurrentState {
                    when (this) {
                        is SettingsUiState.User -> copy(
                            isLoading = false,
                            error = result.error.toSettingsError(),
                        )

                        is SettingsUiState.Restaurant -> copy(
                            isLoading = false,
                            error = result.error.toSettingsError(),
                        )
                    }
                }
            }
        }
    }

    private fun requestAccountDeletion() {
        if (_uiState.value.isLoading || deletionRequestInProgress) return
        deletionRequestInProgress = true
        viewModelScope.launch {
            try {
                when (val result = requestAccountDeletionUseCase()) {
                    is RepositoryResult.Success -> {
                        updateCurrentState {
                            when (this) {
                                is SettingsUiState.User -> copy(error = null)
                                is SettingsUiState.Restaurant -> copy(error = null)
                            }
                        }
                        eventChannel.send(SettingsEvent.DeletionRequested)
                    }
                    is RepositoryResult.Failure -> updateCurrentState {
                        when (this) {
                            is SettingsUiState.User -> copy(error = result.error.toSettingsError())
                            is SettingsUiState.Restaurant -> copy(error = result.error.toSettingsError())
                        }
                    }
                }
            } finally {
                deletionRequestInProgress = false
            }
        }
    }

    private fun emitEvent(event: SettingsEvent) {
        check(eventChannel.trySend(event).isSuccess) { "Could not emit the settings event." }
    }

    private fun updateOpeningHours(
        day: OpeningDay,
        transform: OpeningHoursUiState.() -> OpeningHoursUiState,
    ) = editRestaurant {
        copy(
            openingHours = openingHours.map { hours ->
                if (hours.day == day) hours.transform() else hours
            },
        )
    }

    private fun updateRestaurant(
        transform: SettingsUiState.Restaurant.() -> SettingsUiState.Restaurant,
    ) {
        _uiState.update { state ->
            if (state is SettingsUiState.Restaurant) state.transform() else state
        }
    }

    private fun editRestaurant(
        transform: SettingsUiState.Restaurant.() -> SettingsUiState.Restaurant,
    ) = updateRestaurant {
        transform().copy(error = null, saveSucceeded = false)
    }

    private fun updateCurrentState(transform: SettingsUiState.() -> SettingsUiState) {
        _uiState.update { it.transform() }
    }
}

private fun RepositoryError.toSettingsError(): SettingsError = when (this) {
    RepositoryError.InvalidCredentials -> SettingsError.InvalidCredentials
    RepositoryError.Offline -> SettingsError.Offline
    RepositoryError.Unauthenticated -> SettingsError.Unauthenticated
    RepositoryError.Forbidden -> SettingsError.Forbidden
    is RepositoryError.Unavailable -> SettingsError.TemporarilyUnavailable
    is RepositoryError.AlreadyExists -> SettingsError.AlreadyExists
    is RepositoryError.NotFound -> SettingsError.NotFound
    is RepositoryError.Conflict, is RepositoryError.Validation -> SettingsError.Unknown
}
