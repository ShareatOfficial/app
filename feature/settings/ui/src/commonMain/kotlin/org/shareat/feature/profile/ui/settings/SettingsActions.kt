package org.shareat.feature.profile.ui.settings

enum class SettingsUserAction {
    EditProfile,
    LogOut,
}

sealed interface SettingsRestaurantAction {
    data class NameChanged(val value: String) : SettingsRestaurantAction
    data class DescriptionChanged(val value: String) : SettingsRestaurantAction
    data class PhoneChanged(val value: String) : SettingsRestaurantAction
    data class EmailChanged(val value: String) : SettingsRestaurantAction
    data class StreetChanged(val value: String) : SettingsRestaurantAction
    data class CityChanged(val value: String) : SettingsRestaurantAction
    data class PostcodeChanged(val value: String) : SettingsRestaurantAction
    data class VisibilityChanged(val value: Boolean) : SettingsRestaurantAction
    data class OpeningDayChanged(
        val day: OpeningDay,
        val isOpen: Boolean,
    ) : SettingsRestaurantAction
    data class OpeningTimeChanged(
        val day: OpeningDay,
        val value: String,
    ) : SettingsRestaurantAction
    data class ClosingTimeChanged(
        val day: OpeningDay,
        val value: String,
    ) : SettingsRestaurantAction

    data object Subscription : SettingsRestaurantAction
    data object SaveChanges : SettingsRestaurantAction
    data object LogOut : SettingsRestaurantAction
}
