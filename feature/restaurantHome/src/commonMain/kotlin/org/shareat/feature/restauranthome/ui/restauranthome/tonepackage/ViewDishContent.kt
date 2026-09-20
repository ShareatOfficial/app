package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.shared.designsystem.components.RatingBadge
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergens
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_no_allergen_information
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_reviews
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_unrated
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_write_review

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ViewDishContent(
    dish: RestaurantDish,
    onReviewClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val priceUnits = dish.priceMinorUnits / 100
    val priceCents = (dish.priceMinorUnits % 100).toString().padStart(2, '0')

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RestaurantImage(
            imageUrl = dish.imageUrl,
            contentDescription = dish.imageDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = dish.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
            )
            Column(horizontalAlignment = Alignment.End) {
                dish.ratingLabel?.let { rating ->
                    RatingBadge(ratingLabel = rating)
                } ?: Text(
                    text = stringResource(Res.string.restaurant_home_unrated),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pluralStringResource(
                        Res.plurals.restaurant_home_reviews,
                        dish.reviewCount,
                        dish.reviewCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = dish.description.orEmpty(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    Res.string.restaurant_home_price,
                    priceUnits,
                    priceCents,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.restaurant_home_allergens),
                style = MaterialTheme.typography.titleSmall,
            )
            if (dish.allergens.isEmpty()) {
                Text(
                    text = stringResource(Res.string.restaurant_home_no_allergen_information),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dish.allergens.sortedBy { it.ordinal }.forEach { allergen ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = allergen.label(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }

        onReviewClick?.let {
            Button(
                onClick = onReviewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(Res.string.restaurant_home_write_review))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ViewDishContentPreview() {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant

        ViewDishContent(
            dish = restaurant.dishes.first(),
            onReviewClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}