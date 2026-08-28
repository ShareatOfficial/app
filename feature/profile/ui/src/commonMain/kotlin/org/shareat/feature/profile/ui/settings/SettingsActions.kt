package org.shareat.feature.profile.ui.settings

enum class SettingsUserAction {
    EditProfile,
    PasswordAndSecurity,
    Notifications,
    Privacy,
    ConnectedAccounts,
    ReviewHistory,
    DownloadData,
    DeleteAccount,
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

    data object AdjustMapPin : SettingsRestaurantAction
    data object SpecialDatesAndHolidays : SettingsRestaurantAction
    data object AddSplitHours : SettingsRestaurantAction
    data object ReservationsAndOrderLinks : SettingsRestaurantAction
    data object MenuManagement : SettingsRestaurantAction
    data object PhotosAndMedia : SettingsRestaurantAction
    data object Notifications : SettingsRestaurantAction
    data object TeamAndPermissions : SettingsRestaurantAction
    data object SaveChanges : SettingsRestaurantAction
    data object LogOut : SettingsRestaurantAction
}
