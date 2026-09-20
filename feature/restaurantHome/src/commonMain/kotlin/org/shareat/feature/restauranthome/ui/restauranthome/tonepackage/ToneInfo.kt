package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun RestaurantInfo(
    restaurant: RestaurantHomeData,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditMainInfoClick: () -> Unit = {},
    isEditMode: Boolean = false

) {
    Column(
        modifier = modifier
            .fillMaxWidth().background(color = MaterialTheme.colorScheme.background)
    ) {
        // Image + main info box with the image the restaurant choose behind.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 297.dp) // ~1/3 of a 891dp tall screen like Pixel 9
        ) {
            RestaurantImage(
                imageUrl = restaurant.imageUrl,
                contentDescription = restaurant.imageDescription,
                modifier = Modifier.fillMaxWidth().matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = restaurant.description.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
            // We map it inside a custom component to avoid ColumnScope extension propagation.
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = isEditMode,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    label = "edit_main_info_button_animation"
                ) {
                    IconButton(
                        onClick = onEditMainInfoClick,
                        modifier = Modifier.padding(16.dp),
                        colors = IconButtonDefaults.iconButtonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Secondary info.  Street, rating, if it is open, etc...
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeRestaurantActionRow(
                icon = Icons.Outlined.LocationOn,
                text = restaurant.address.streetLine,
                onClick = onOpenDirectionsClick,  // A better thing should return a type Actions or restaurant Info.
                trailingContent = {}
            )
            HomeRestaurantActionRow(
                icon = Icons.Filled.Star,
                text = restaurant.ratingLabel ?: "Sin calificación",
                onClick = onRateClick,
                trailingContent = {}
            )
        }
    }
}


@Composable
private fun HomeRestaurantActionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(modifier = Modifier.padding(16.dp).clickable(onClick = onClick)) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        trailingContent()
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun RestaurantInfoPreview() {
    RestaurantInfoPreviewContent(isEditMode = false)
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun RestaurantInfoEditModePreview() {
    RestaurantInfoPreviewContent(isEditMode = true)
}

@Composable
private fun RestaurantInfoPreviewContent(isEditMode: Boolean) {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant

        RestaurantInfo(
            restaurant = restaurant,
            onOpenDirectionsClick = {},
            onRateClick = {},
            isEditMode = isEditMode,
        )
    }
}

