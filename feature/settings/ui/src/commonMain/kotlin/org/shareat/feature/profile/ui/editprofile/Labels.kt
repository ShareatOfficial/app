package org.shareat.feature.profile.ui.editprofile

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import shareat.feature.settings.ui.generated.resources.Res
import shareat.feature.settings.ui.generated.resources.edit_profile_error_already_exists
import shareat.feature.settings.ui.generated.resources.edit_profile_error_credentials
import shareat.feature.settings.ui.generated.resources.edit_profile_error_customer_only
import shareat.feature.settings.ui.generated.resources.edit_profile_error_forbidden
import shareat.feature.settings.ui.generated.resources.edit_profile_error_generic
import shareat.feature.settings.ui.generated.resources.edit_profile_error_not_found
import shareat.feature.settings.ui.generated.resources.edit_profile_error_offline
import shareat.feature.settings.ui.generated.resources.edit_profile_error_session
import shareat.feature.settings.ui.generated.resources.edit_profile_error_unavailable

@Composable
internal fun EditProfileError.label(): String = stringResource(
    when (this) {
        EditProfileError.CUSTOMER_ONLY -> Res.string.edit_profile_error_customer_only
        EditProfileError.INVALID_CREDENTIALS -> Res.string.edit_profile_error_credentials
        EditProfileError.OFFLINE -> Res.string.edit_profile_error_offline
        EditProfileError.UNAUTHENTICATED -> Res.string.edit_profile_error_session
        EditProfileError.FORBIDDEN -> Res.string.edit_profile_error_forbidden
        EditProfileError.TEMPORARILY_UNAVAILABLE -> Res.string.edit_profile_error_unavailable
        EditProfileError.ALREADY_EXISTS -> Res.string.edit_profile_error_already_exists
        EditProfileError.NOT_FOUND -> Res.string.edit_profile_error_not_found
        EditProfileError.UNKNOWN -> Res.string.edit_profile_error_generic
    },
)
