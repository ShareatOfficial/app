package org.shareat.feature.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

sealed interface SettingsUiState {
    data class User(
        val name: String = "Alex Rivera",
        val email: String = "alex.rivera@example.com",
        val initials: String = "AR",
    ) : SettingsUiState

    data class Restaurant(
        val name: String = "Osteria Bella",
        val cuisine: String = "Modern Italian",
        val description: String =
            "A contemporary approach to traditional Italian family recipes.",
        val phone: String = "+1 (555) 123-4567",
        val email: String = "hello@osteriabella.com",
        val website: String = "https://osteriabella.com",
        val streetAddress: String = "1284 Culinary Blvd",
        val city: String = "Portland",
        val postcode: String = "97205",
        val isPublished: Boolean = true,
        val openingHours: List<OpeningHoursUiState> = defaultOpeningHours(),
    ) : SettingsUiState
}

data class OpeningHoursUiState(
    val day: String,
    val isOpen: Boolean,
    val openingTime: String = "11:00",
    val closingTime: String = "22:00",
)

private fun defaultOpeningHours() = listOf(
    OpeningHoursUiState(day = "Monday", isOpen = false),
    OpeningHoursUiState(day = "Tuesday", isOpen = true),
    OpeningHoursUiState(day = "Wednesday", isOpen = true),
    OpeningHoursUiState(day = "Thursday", isOpen = true),
    OpeningHoursUiState(day = "Friday", isOpen = true, closingTime = "23:00"),
    OpeningHoursUiState(day = "Saturday", isOpen = true, closingTime = "23:00"),
    OpeningHoursUiState(day = "Sunday", isOpen = true, closingTime = "21:00"),
)

@KoinViewModel
class EditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.User())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun showUserSettings() {
        _uiState.value = SettingsUiState.User()
    }

    fun showRestaurantSettings() {
        _uiState.value = SettingsUiState.Restaurant()
    }

    fun onRestaurantNameChange(value: String) = updateRestaurant { copy(name = value) }

    fun onRestaurantCuisineChange(value: String) = updateRestaurant { copy(cuisine = value) }

    fun onRestaurantDescriptionChange(value: String) =
        updateRestaurant { copy(description = value) }

    fun onRestaurantPhoneChange(value: String) = updateRestaurant { copy(phone = value) }

    fun onRestaurantEmailChange(value: String) = updateRestaurant { copy(email = value) }

    fun onRestaurantWebsiteChange(value: String) = updateRestaurant { copy(website = value) }

    fun onRestaurantStreetChange(value: String) = updateRestaurant { copy(streetAddress = value) }

    fun onRestaurantCityChange(value: String) = updateRestaurant { copy(city = value) }

    fun onRestaurantPostcodeChange(value: String) = updateRestaurant { copy(postcode = value) }

    fun onRestaurantVisibilityChange(value: Boolean) =
        updateRestaurant { copy(isPublished = value) }

    fun onOpeningDayChange(day: String, isOpen: Boolean) = updateOpeningHours(day) {
        copy(isOpen = isOpen)
    }

    fun onOpeningTimeChange(day: String, value: String) = updateOpeningHours(day) {
        copy(openingTime = value)
    }

    fun onClosingTimeChange(day: String, value: String) = updateOpeningHours(day) {
        copy(closingTime = value)
    }

    fun onSaveClick() = Unit

    fun onCloseSession() = Unit

    private fun updateOpeningHours(
        day: String,
        transform: OpeningHoursUiState.() -> OpeningHoursUiState,
    ) = updateRestaurant {
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
}
