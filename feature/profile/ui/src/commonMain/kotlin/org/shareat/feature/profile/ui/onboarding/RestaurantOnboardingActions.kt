package org.shareat.feature.profile.ui.onboarding

import org.shareat.app.domain.model.Weekday

sealed interface RestaurantOnboardingAction {
    data class NameChanged(val value: String) : RestaurantOnboardingAction
    data class DescriptionChanged(val value: String) : RestaurantOnboardingAction
    data class EmailChanged(val value: String) : RestaurantOnboardingAction
    data class PhoneChanged(val value: String) : RestaurantOnboardingAction
    data class StreetChanged(val value: String) : RestaurantOnboardingAction
    data class CityChanged(val value: String) : RestaurantOnboardingAction
    data class PostcodeChanged(val value: String) : RestaurantOnboardingAction
    data class ProvinceChanged(val value: String) : RestaurantOnboardingAction
    data class DayEnabledChanged(val day: Weekday, val enabled: Boolean) : RestaurantOnboardingAction
    data class OpensAtChanged(val day: Weekday, val value: String) : RestaurantOnboardingAction
    data class ClosesAtChanged(val day: Weekday, val value: String) : RestaurantOnboardingAction
    data object Submit : RestaurantOnboardingAction
    data object Logout : RestaurantOnboardingAction
}

sealed interface RestaurantOnboardingEvent {
    data object Completed : RestaurantOnboardingEvent
    data object LogoutSuccess : RestaurantOnboardingEvent
}
