package org.shareat.feature.profile.ui.onboarding

import org.shareat.app.domain.model.Weekday

data class RestaurantOnboardingUiState(
    val name: String = "",
    val description: String = "",
    val publicEmail: String = "",
    val publicPhone: String = "",
    val street: String = "",
    val city: String = "",
    val postcode: String = "",
    val province: String = "",
    val hours: List<OnboardingOpeningHours> = Weekday.entries.map(::OnboardingOpeningHours),
    val errors: OnboardingFieldErrors = OnboardingFieldErrors(),
    val isSubmitting: Boolean = false,
    val submitError: OnboardingSubmitError? = null,
)

data class OnboardingOpeningHours(
    val day: Weekday,
    val enabled: Boolean = true,
    val opensAt: String = "11:00",
    val closesAt: String = "22:00",
    val error: OnboardingHoursError? = null,
)

data class OnboardingFieldErrors(
    val name: OnboardingFieldError? = null,
    val email: OnboardingFieldError? = null,
    val street: OnboardingFieldError? = null,
    val city: OnboardingFieldError? = null,
    val postcode: OnboardingFieldError? = null,
) {
    val hasErrors: Boolean
        get() = listOf(name, email, street, city, postcode).any { it != null }
}
