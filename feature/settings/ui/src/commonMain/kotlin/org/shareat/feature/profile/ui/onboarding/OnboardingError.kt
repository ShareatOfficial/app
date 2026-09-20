package org.shareat.feature.profile.ui.onboarding

enum class OnboardingFieldError {
    NAME_REQUIRED,
    STREET_REQUIRED,
    CITY_REQUIRED,
    POSTCODE_REQUIRED,
    POSTCODE_INVALID,
    EMAIL_INVALID,
}

enum class OnboardingHoursError {
    TIME_FORMAT,
    SAME_TIMES,
}

enum class OnboardingSubmitError {
    OFFLINE,
    UNAUTHENTICATED,
    FORBIDDEN,
    ALREADY_EXISTS,
    NOT_FOUND,
    TEMPORARILY_UNAVAILABLE,
    UNKNOWN,
}
