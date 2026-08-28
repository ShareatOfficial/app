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
    val imageAlternativeText: String = "",
    val image: ProcessedRestaurantImage? = null,
    val hours: List<OnboardingOpeningHours> = Weekday.entries.map(::OnboardingOpeningHours),
    val errors: OnboardingFieldErrors = OnboardingFieldErrors(),
    val isProcessingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val createdRestaurantId: String? = null,
    val imageUploadFailed: Boolean = false,
    val errorMessage: String? = null,
)

data class ProcessedRestaurantImage(
    val displayName: String,
    val bytes: ByteArray,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean = other is ProcessedRestaurantImage &&
        displayName == other.displayName && mimeType == other.mimeType && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = 31 * displayName.hashCode() + bytes.contentHashCode()
}

data class OnboardingOpeningHours(
    val day: Weekday,
    val enabled: Boolean = false,
    val opensAt: String = "11:00",
    val closesAt: String = "22:00",
    val error: String? = null,
)

data class OnboardingFieldErrors(
    val name: String? = null,
    val email: String? = null,
    val street: String? = null,
    val city: String? = null,
    val postcode: String? = null,
    val image: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(name, email, street, city, postcode, image).any { it != null }
}
