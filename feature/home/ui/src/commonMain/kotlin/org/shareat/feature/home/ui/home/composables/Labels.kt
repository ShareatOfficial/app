package org.shareat.feature.home.ui.home.composables

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.home.ui.home.model.HomeError
import shareat.feature.home.ui.generated.resources.Res
import shareat.feature.home.ui.generated.resources.home_error_already_exists
import shareat.feature.home.ui.generated.resources.home_error_credentials
import shareat.feature.home.ui.generated.resources.home_error_forbidden
import shareat.feature.home.ui.generated.resources.home_error_generic
import shareat.feature.home.ui.generated.resources.home_error_not_found
import shareat.feature.home.ui.generated.resources.home_error_offline
import shareat.feature.home.ui.generated.resources.home_error_session
import shareat.feature.home.ui.generated.resources.home_error_unavailable
import shareat.feature.home.ui.generated.resources.home_unrated

@Composable
internal fun HomeError.label(): String = stringResource(
    when (this) {
        HomeError.INVALID_CREDENTIALS -> Res.string.home_error_credentials
        HomeError.OFFLINE -> Res.string.home_error_offline
        HomeError.UNAUTHENTICATED -> Res.string.home_error_session
        HomeError.FORBIDDEN -> Res.string.home_error_forbidden
        HomeError.TEMPORARILY_UNAVAILABLE -> Res.string.home_error_unavailable
        HomeError.ALREADY_EXISTS -> Res.string.home_error_already_exists
        HomeError.NOT_FOUND -> Res.string.home_error_not_found
        HomeError.UNKNOWN -> Res.string.home_error_generic
    },
)

@Composable
internal fun ratingLabelOrUnrated(ratingLabel: String?): String =
    ratingLabel ?: stringResource(Res.string.home_unrated)
