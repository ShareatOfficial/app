package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restaurant.ui.model.RestaurantHeaderUiState
import org.shareat.shared.designsystem.components.restaurant.RestaurantHeader
import org.shareat.shared.designsystem.components.restaurant.RestaurantImage
import org.shareat.shared.designsystem.components.restaurant.RestaurantInfoAction
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_address_unavailable
import shareat.feature.restaurant.ui.generated.resources.restaurant_unrated

@Composable
internal fun RestaurantPublicHeader(
    header: RestaurantHeaderUiState,
    onAddressClick: () -> Unit,
    onRatingClick: () -> Unit,
) {
    RestaurantHeader(
        name = header.name,
        description = header.description.orEmpty(),
        image = { imageModifier ->
            RestaurantImage(
                imageUrl = header.heroImageUrl,
                contentDescription = header.heroImageDescription,
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
            )
        },
        info = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RestaurantInfoAction(
                    icon = Icons.Outlined.LocationOn,
                    text = header.address.ifBlank { stringResource(Res.string.restaurant_address_unavailable) },
                    onClick = onAddressClick,
                    modifier = Modifier.weight(1f),
                )
                RestaurantInfoAction(
                    icon = Icons.Filled.Star,
                    text = header.ratingLabel ?: stringResource(Res.string.restaurant_unrated),
                    onClick = onRatingClick,
                    modifier = Modifier.weight(0.5f),
                )
            }
        },
    )
}
