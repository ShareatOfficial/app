package org.shareat.feature.profile.ui.settings

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import org.shareat.app.domain.model.Weekday
import shareat.feature.settings.ui.generated.resources.Res
import shareat.feature.settings.ui.generated.resources.settings_error_address_incomplete
import shareat.feature.settings.ui.generated.resources.settings_error_address_required_to_publish
import shareat.feature.settings.ui.generated.resources.settings_error_already_exists
import shareat.feature.settings.ui.generated.resources.settings_error_closing_time_format
import shareat.feature.settings.ui.generated.resources.settings_error_credentials
import shareat.feature.settings.ui.generated.resources.settings_error_forbidden
import shareat.feature.settings.ui.generated.resources.settings_error_generic
import shareat.feature.settings.ui.generated.resources.settings_error_invalid_email
import shareat.feature.settings.ui.generated.resources.settings_error_name_required
import shareat.feature.settings.ui.generated.resources.settings_error_not_found
import shareat.feature.settings.ui.generated.resources.settings_error_offline
import shareat.feature.settings.ui.generated.resources.settings_error_opening_time_format
import shareat.feature.settings.ui.generated.resources.settings_error_restaurant_unavailable
import shareat.feature.settings.ui.generated.resources.settings_error_same_times
import shareat.feature.settings.ui.generated.resources.settings_error_session
import shareat.feature.settings.ui.generated.resources.settings_error_unavailable
import shareat.feature.settings.ui.generated.resources.settings_language_english
import shareat.feature.settings.ui.generated.resources.settings_language_next_launch
import shareat.feature.settings.ui.generated.resources.settings_language_spanish
import shareat.feature.settings.ui.generated.resources.settings_language_system
import shareat.feature.settings.ui.generated.resources.settings_language_unsupported
import shareat.feature.settings.ui.generated.resources.weekday_friday
import shareat.feature.settings.ui.generated.resources.weekday_monday
import shareat.feature.settings.ui.generated.resources.weekday_saturday
import shareat.feature.settings.ui.generated.resources.weekday_sunday
import shareat.feature.settings.ui.generated.resources.weekday_thursday
import shareat.feature.settings.ui.generated.resources.weekday_tuesday
import shareat.feature.settings.ui.generated.resources.weekday_wednesday

internal fun Weekday.labelResource(): StringResource = when (this) {
    Weekday.Monday -> Res.string.weekday_monday
    Weekday.Tuesday -> Res.string.weekday_tuesday
    Weekday.Wednesday -> Res.string.weekday_wednesday
    Weekday.Thursday -> Res.string.weekday_thursday
    Weekday.Friday -> Res.string.weekday_friday
    Weekday.Saturday -> Res.string.weekday_saturday
    Weekday.Sunday -> Res.string.weekday_sunday
}

@Composable
internal fun OpeningDay.label(): String = stringResource(Weekday.entries[ordinal].labelResource())

@Composable
internal fun SettingsError.label(): String = when (this) {
    is SettingsError.InvalidOpeningTime ->
        stringResource(Res.string.settings_error_opening_time_format, day.label())

    is SettingsError.InvalidClosingTime ->
        stringResource(Res.string.settings_error_closing_time_format, day.label())

    is SettingsError.SameOpeningAndClosingTime ->
        stringResource(Res.string.settings_error_same_times, day.label())

    else -> stringResource(
        when (this) {
            SettingsError.RestaurantUnavailable -> Res.string.settings_error_restaurant_unavailable
            SettingsError.NameRequired -> Res.string.settings_error_name_required
            SettingsError.AddressIncomplete -> Res.string.settings_error_address_incomplete
            SettingsError.InvalidEmail -> Res.string.settings_error_invalid_email
            SettingsError.AddressRequiredToPublish ->
                Res.string.settings_error_address_required_to_publish

            SettingsError.InvalidCredentials -> Res.string.settings_error_credentials
            SettingsError.Offline -> Res.string.settings_error_offline
            SettingsError.Unauthenticated -> Res.string.settings_error_session
            SettingsError.Forbidden -> Res.string.settings_error_forbidden
            SettingsError.TemporarilyUnavailable -> Res.string.settings_error_unavailable
            SettingsError.AlreadyExists -> Res.string.settings_error_already_exists
            SettingsError.NotFound -> Res.string.settings_error_not_found
            else -> Res.string.settings_error_generic
        },
    )
}

@Composable
internal fun AppLanguage.label(): String = stringResource(
    when (this) {
        AppLanguage.System -> Res.string.settings_language_system
        AppLanguage.English -> Res.string.settings_language_english
        AppLanguage.Spanish -> Res.string.settings_language_spanish
    },
)

/** The note shown under the picker when the platform cannot switch the running app. */
@Composable
internal fun AppLanguageUiState.appliesLaterNoticeOrNull(): String? = when (support) {
    AppLanguageSelectionSupport.IMMEDIATE -> null
    AppLanguageSelectionSupport.NEXT_LAUNCH -> stringResource(Res.string.settings_language_next_launch)
    AppLanguageSelectionSupport.UNSUPPORTED -> stringResource(Res.string.settings_language_unsupported)
}
