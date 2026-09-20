package org.shareat.feature.lastactivity.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import shareat.feature.lastactivity.generated.resources.Res
import shareat.feature.lastactivity.generated.resources.last_activity_error_generic
import shareat.feature.lastactivity.generated.resources.last_activity_error_offline
import shareat.feature.lastactivity.generated.resources.last_activity_error_session
import shareat.feature.lastactivity.generated.resources.last_activity_error_unavailable
import shareat.feature.lastactivity.generated.resources.last_activity_type_dish
import shareat.feature.lastactivity.generated.resources.last_activity_type_restaurant

@Composable
internal fun LastActivityError.label(): String = stringResource(
    when (this) {
        LastActivityError.OFFLINE -> Res.string.last_activity_error_offline
        LastActivityError.UNAUTHENTICATED -> Res.string.last_activity_error_session
        LastActivityError.TEMPORARILY_UNAVAILABLE -> Res.string.last_activity_error_unavailable
        LastActivityError.UNKNOWN -> Res.string.last_activity_error_generic
    },
)

@Composable
internal fun LastActivityTargetType.label(): String = stringResource(
    when (this) {
        LastActivityTargetType.DISH -> Res.string.last_activity_type_dish
        LastActivityTargetType.RESTAURANT -> Res.string.last_activity_type_restaurant
    },
)
