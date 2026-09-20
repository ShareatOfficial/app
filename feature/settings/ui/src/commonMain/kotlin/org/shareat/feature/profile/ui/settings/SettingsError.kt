package org.shareat.feature.profile.ui.settings

sealed interface SettingsError {
    data object RestaurantUnavailable : SettingsError
    data object NameRequired : SettingsError
    data object AddressIncomplete : SettingsError
    data object InvalidEmail : SettingsError
    data object AddressRequiredToPublish : SettingsError
    data class InvalidOpeningTime(val day: OpeningDay) : SettingsError
    data class InvalidClosingTime(val day: OpeningDay) : SettingsError
    data class SameOpeningAndClosingTime(val day: OpeningDay) : SettingsError
    data object InvalidCredentials : SettingsError
    data object Offline : SettingsError
    data object Unauthenticated : SettingsError
    data object Forbidden : SettingsError
    data object TemporarilyUnavailable : SettingsError
    data object AlreadyExists : SettingsError
    data object NotFound : SettingsError
    data object Unknown : SettingsError
}
