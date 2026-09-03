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
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.SignOutUseCase
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase

@Stable
@KoinViewModel
class SettingsViewModel(
    private val loadProfileSettingsUseCase: LoadProfileSettingsUseCase,
    private val updateRestaurantInfoUseCase: UpdateRestaurantInfoUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    private val eventChannel = Channel<SettingsEvent>(capacity = Channel.BUFFERED)
    internal val events: Flow<SettingsEvent> = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow<SettingsUiState>(
        SettingsUiState.User(isLoading = true),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var loadedRestaurant: Restaurant? = null

    init {
        loadSettings()
    }

    fun onUserAction(action: SettingsUserAction) {
        when (action) {
            SettingsUserAction.EditProfile -> emitEvent(SettingsEvent.NavigateToEditProfile)
            SettingsUserAction.PasswordAndSecurity -> openPasswordAndSecurity()
            SettingsUserAction.Notifications -> openUserNotifications()
            SettingsUserAction.Privacy -> openPrivacy()
            SettingsUserAction.ConnectedAccounts -> openConnectedAccounts()
            SettingsUserAction.ReviewHistory -> openReviewHistory()
            SettingsUserAction.DownloadData -> downloadData()
            SettingsUserAction.DeleteAccount -> deleteAccount()
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

            SettingsRestaurantAction.AdjustMapPin -> adjustMapPin()
            SettingsRestaurantAction.SpecialDatesAndHolidays -> openSpecialDatesAndHolidays()
            SettingsRestaurantAction.AddSplitHours -> addSplitHours()
            SettingsRestaurantAction.ReservationsAndOrderLinks -> openReservationsAndOrderLinks()
            SettingsRestaurantAction.PhotosAndMedia -> openPhotosAndMedia()
            SettingsRestaurantAction.Notifications -> openRestaurantNotifications()
            SettingsRestaurantAction.TeamAndPermissions -> openTeamAndPermissions()
            SettingsRestaurantAction.SaveChanges -> saveRestaurantChanges()
            SettingsRestaurantAction.LogOut -> onLogOut()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            when (val result = loadProfileSettingsUseCase()) {
                is RepositoryResult.Success -> when (val settings = result.value) {
                    is ProfileSettings.User -> _uiState.value = settings.toUiState()
                    is ProfileSettings.RestaurantOwner -> {
                        loadedRestaurant = settings.restaurant
                        _uiState.value = settings.toUiState()
                    }
                }

                is RepositoryResult.Failure -> updateCurrentState {
                    when (this) {
                        is SettingsUiState.User -> copy(
                            isLoading = false,
                            errorMessage = result.error.toUserMessage(),
                        )

                        is SettingsUiState.Restaurant -> copy(
                            isLoading = false,
                            errorMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun openPasswordAndSecurity(): Nothing = TODO("To be implemented")
    private fun openUserNotifications(): Nothing = TODO("To be implemented")
    private fun openPrivacy(): Nothing = TODO("To be implemented")
    private fun openConnectedAccounts(): Nothing = TODO("To be implemented")
    private fun openReviewHistory(): Nothing = TODO("To be implemented")
    private fun downloadData(): Nothing = TODO("To be implemented")
    private fun deleteAccount(): Nothing = TODO("To be implemented")

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

    private fun adjustMapPin(): Nothing = TODO("To be implemented")
    private fun openSpecialDatesAndHolidays(): Nothing = TODO("To be implemented")
    private fun addSplitHours(): Nothing = TODO("To be implemented")
    private fun openReservationsAndOrderLinks(): Nothing = TODO("To be implemented")
    private fun openPhotosAndMedia(): Nothing = TODO("To be implemented")
    private fun openRestaurantNotifications(): Nothing = TODO("To be implemented")
    private fun openTeamAndPermissions(): Nothing = TODO("To be implemented")

    private fun saveRestaurantChanges() {
        val state = _uiState.value as? SettingsUiState.Restaurant ?: return
        val original = loadedRestaurant ?: return updateRestaurant {
            copy(errorMessage = "Restaurant data is not available.")
        }
        when (val mapping = state.toUpdateParams(original)) {
            is RestaurantSettingsMappingResult.Failure -> updateRestaurant {
                copy(errorMessage = mapping.message, saveSucceeded = false)
            }

            is RestaurantSettingsMappingResult.Success -> viewModelScope.launch {
                updateRestaurant {
                    copy(
                        isSaving = true,
                        errorMessage = null,
                        saveSucceeded = false
                    )
                }
                when (val result = updateRestaurantInfoUseCase(mapping.params)) {
                    is RepositoryResult.Success -> {
                        loadedRestaurant = result.value
                        _uiState.value = result.value.toUiState().copy(saveSucceeded = true)
                    }

                    is RepositoryResult.Failure -> updateRestaurant {
                        copy(
                            isSaving = false,
                            errorMessage = result.error.toUserMessage(),
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
                is SettingsUiState.User -> copy(isLoading = true, errorMessage = null)
                is SettingsUiState.Restaurant -> copy(isLoading = true, errorMessage = null)
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
                            errorMessage = result.error.toUserMessage(),
                        )

                        is SettingsUiState.Restaurant -> copy(
                            isLoading = false,
                            errorMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun emitEvent(event: SettingsEvent) {
        check(eventChannel.trySend(event).isSuccess) { "Unable to emit settings event." }
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
        transform().copy(errorMessage = null, saveSucceeded = false)
    }

    private fun updateCurrentState(transform: SettingsUiState.() -> SettingsUiState) {
        _uiState.update { it.transform() }
    }
}

private fun RepositoryError.toUserMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "Your session credentials are no longer valid."
    RepositoryError.Offline -> "You appear to be offline. Try again when connected."
    RepositoryError.Unauthenticated -> "Your session has expired. Please sign in again."
    RepositoryError.Forbidden -> "This account is not allowed to perform that action."
    is RepositoryError.Unavailable -> "The service is temporarily unavailable."
    is RepositoryError.AlreadyExists -> "The ${entity} already exists."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "The requested ${entity} could not be found."
    is RepositoryError.Validation -> reason
}
