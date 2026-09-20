package org.shareat.feature.profile.ui.onboarding

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import shareat.feature.settings.ui.generated.resources.Res
import shareat.feature.settings.ui.generated.resources.onboarding_error_already_exists
import shareat.feature.settings.ui.generated.resources.onboarding_error_city_required
import shareat.feature.settings.ui.generated.resources.onboarding_error_email_invalid
import shareat.feature.settings.ui.generated.resources.onboarding_error_forbidden
import shareat.feature.settings.ui.generated.resources.onboarding_error_generic
import shareat.feature.settings.ui.generated.resources.onboarding_error_name_required
import shareat.feature.settings.ui.generated.resources.onboarding_error_not_found
import shareat.feature.settings.ui.generated.resources.onboarding_error_offline
import shareat.feature.settings.ui.generated.resources.onboarding_error_postcode_invalid
import shareat.feature.settings.ui.generated.resources.onboarding_error_postcode_required
import shareat.feature.settings.ui.generated.resources.onboarding_error_same_times
import shareat.feature.settings.ui.generated.resources.onboarding_error_session
import shareat.feature.settings.ui.generated.resources.onboarding_error_street_required
import shareat.feature.settings.ui.generated.resources.onboarding_error_time_format
import shareat.feature.settings.ui.generated.resources.onboarding_error_unavailable

@Composable
internal fun OnboardingFieldError.label(): String = stringResource(
    when (this) {
        OnboardingFieldError.NAME_REQUIRED -> Res.string.onboarding_error_name_required
        OnboardingFieldError.STREET_REQUIRED -> Res.string.onboarding_error_street_required
        OnboardingFieldError.CITY_REQUIRED -> Res.string.onboarding_error_city_required
        OnboardingFieldError.POSTCODE_REQUIRED -> Res.string.onboarding_error_postcode_required
        OnboardingFieldError.POSTCODE_INVALID -> Res.string.onboarding_error_postcode_invalid
        OnboardingFieldError.EMAIL_INVALID -> Res.string.onboarding_error_email_invalid
    },
)

@Composable
internal fun OnboardingHoursError.label(): String = stringResource(
    when (this) {
        OnboardingHoursError.TIME_FORMAT -> Res.string.onboarding_error_time_format
        OnboardingHoursError.SAME_TIMES -> Res.string.onboarding_error_same_times
    },
)

@Composable
internal fun OnboardingSubmitError.label(): String = stringResource(
    when (this) {
        OnboardingSubmitError.OFFLINE -> Res.string.onboarding_error_offline
        OnboardingSubmitError.UNAUTHENTICATED -> Res.string.onboarding_error_session
        OnboardingSubmitError.FORBIDDEN -> Res.string.onboarding_error_forbidden
        OnboardingSubmitError.ALREADY_EXISTS -> Res.string.onboarding_error_already_exists
        OnboardingSubmitError.NOT_FOUND -> Res.string.onboarding_error_not_found
        OnboardingSubmitError.TEMPORARILY_UNAVAILABLE -> Res.string.onboarding_error_unavailable
        OnboardingSubmitError.UNKNOWN -> Res.string.onboarding_error_generic
    },
)
