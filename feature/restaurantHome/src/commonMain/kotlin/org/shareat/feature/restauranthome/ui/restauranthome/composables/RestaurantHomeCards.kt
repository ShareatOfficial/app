package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantDishUiState
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_disabled
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_edit
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_enabled
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_reviews
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_unrated
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_image_placeholder
import shareat.feature.restauranthome.ui.generated.resources.restaurant_placeholder_hero

@Composable
internal fun RestaurantHeaderCard(
    name: String,
    description: String?,
    imageUrl: String?,
    imageDescription: String?,
    address: String,
    ratingLabel: String?,
    reviewCount: Int,
    isManagement: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (imageUrl == null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(Res.drawable.restaurant_placeholder_hero),
                        contentDescription = stringResource(Res.string.restaurant_home_image_placeholder),
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = imageDescription,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(
                    text = address,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = name, style = MaterialTheme.typography.headlineMedium)
                description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RatingSummary(ratingLabel = ratingLabel, reviewCount = reviewCount, modifier = Modifier.weight(1f))
                    if (isManagement) {
                        Button(onClick = onEditClick, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                            Text(stringResource(Res.string.restaurant_home_edit))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSummary(ratingLabel: String?, reviewCount: Int, modifier: Modifier = Modifier) {
    if (ratingLabel == null) {
        Text(
            text = stringResource(Res.string.restaurant_home_unrated),
            modifier = modifier,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = ratingLabel, style = MaterialTheme.typography.titleMedium)
        Text(
            text = pluralStringResource(Res.plurals.restaurant_home_reviews, reviewCount, reviewCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun ManagementDishCard(
    dish: RestaurantDishUiState,
    showEdit: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (dish.imageUrl == null) {
                DishImagePlaceholder(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).sizeIn(minWidth = 80.dp, minHeight = 80.dp),
                )
            } else {
                AsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.imageDescription,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).sizeIn(minWidth = 80.dp, minHeight = 80.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = dish.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (showEdit) {
                        IconButton(onClick = onEditClick, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(Res.string.restaurant_home_dish_edit))
                        }
                    }
                }
                dish.description?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(text = dish.priceLabel(), style = MaterialTheme.typography.titleSmall)
                RatingSummary(
                    ratingLabel = dish.ratingLabel,
                    reviewCount = dish.reviewCount,
                )
                if (showEdit) {
                    Surface(
                        color = if (dish.isPublished) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (dish.isPublished) Res.string.restaurant_home_dish_enabled
                                else Res.string.restaurant_home_dish_disabled,
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DishImagePlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Text(
                text = stringResource(Res.string.restaurant_home_image_placeholder),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RestaurantDishUiState.priceLabel(): String = stringResource(
    Res.string.restaurant_home_price,
    priceMinorUnits / 100,
    (priceMinorUnits % 100).toString().padStart(2, '0'),
)
