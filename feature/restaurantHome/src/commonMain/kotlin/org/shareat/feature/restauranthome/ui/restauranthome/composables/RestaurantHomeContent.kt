package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.shared.designsystem.shimmerEffect
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_add_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_empty
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_empty_management
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
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_retry

// TODO to check and work on this states.
@Composable
internal fun ErrorContent(
    error: RestaurantHomeError,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(text = error.message(), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetryClick, modifier = Modifier.height(48.dp)) {
            Text(stringResource(Res.string.restaurant_home_retry))
        }
    }
}

@Composable
internal fun RestaurantHomeEmptyContent(
    isManagement: Boolean = false,
    onAddDishClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(if (isManagement) Res.string.restaurant_home_empty_management else Res.string.restaurant_home_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (isManagement && onAddDishClick != null) {
            Button(onClick = onAddDishClick) {
                Text(stringResource(Res.string.restaurant_home_add_dish))
            }
        }
    }
}

@Composable
internal fun RestaurantHomeSkeleton(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).shimmerEffect(color))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.fillMaxWidth(.65f).height(28.dp)
                        .shimmerEffect(color, RoundedCornerShape(4.dp))
                )
                Box(
                    Modifier.fillMaxWidth().height(18.dp)
                        .shimmerEffect(color, RoundedCornerShape(4.dp))
                )
                Box(
                    Modifier.fillMaxWidth(.4f).height(36.dp)
                        .shimmerEffect(color, RoundedCornerShape(18.dp))
                )
            }
        }
        repeat(3) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.fillMaxWidth(.55f).height(22.dp)
                            .shimmerEffect(color, RoundedCornerShape(4.dp))
                    )
                    Box(
                        Modifier.fillMaxWidth().height(16.dp)
                            .shimmerEffect(color, RoundedCornerShape(4.dp))
                    )
                    Box(
                        Modifier.fillMaxWidth(.25f).height(18.dp)
                            .shimmerEffect(color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantHomeError.message(): String = stringResource(
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
