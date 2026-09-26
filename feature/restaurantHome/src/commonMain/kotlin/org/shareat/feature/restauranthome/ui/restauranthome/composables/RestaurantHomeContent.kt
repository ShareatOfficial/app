package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_forbidden
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_generic
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_format
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_read
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_size
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_upload_after_details_saved
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_not_found
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_offline
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_session
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_unavailable

@Composable
internal fun RestaurantHomeError.message(): String = stringResource(
    when (this) {
        RestaurantHomeError.OFFLINE -> Res.string.restaurant_home_error_offline
        RestaurantHomeError.UNAUTHENTICATED -> Res.string.restaurant_home_error_session
        RestaurantHomeError.FORBIDDEN -> Res.string.restaurant_home_error_forbidden
        RestaurantHomeError.NOT_FOUND -> Res.string.restaurant_home_error_not_found
        RestaurantHomeError.TEMPORARILY_UNAVAILABLE -> Res.string.restaurant_home_error_unavailable
        RestaurantHomeError.IMAGE_FORMAT_UNSUPPORTED -> Res.string.restaurant_home_error_image_format
        RestaurantHomeError.IMAGE_TOO_LARGE -> Res.string.restaurant_home_error_image_size
        RestaurantHomeError.IMAGE_READ_FAILED -> Res.string.restaurant_home_error_image_read
        RestaurantHomeError.IMAGE_UPLOAD_FAILED_AFTER_DETAILS_SAVED ->
            Res.string.restaurant_home_error_image_upload_after_details_saved

        RestaurantHomeError.VALIDATION, RestaurantHomeError.UNKNOWN -> Res.string.restaurant_home_error_generic
    },
)
