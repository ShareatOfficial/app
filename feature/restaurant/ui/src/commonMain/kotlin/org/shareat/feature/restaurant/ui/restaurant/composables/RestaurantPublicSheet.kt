package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.feature.restaurant.ui.model.DishCardUiState
import org.shareat.feature.restaurant.ui.model.RestaurantHeaderUiState
import org.shareat.feature.restaurant.ui.restaurant.composables.dish.DishRatingBar
import org.shareat.feature.restaurant.ui.restaurant.composables.dish.DishReviewsCarousel
import org.shareat.shared.designsystem.components.RatingBadge
import org.shareat.shared.designsystem.components.restaurant.RestaurantDishDetails
import org.shareat.shared.designsystem.components.restaurant.RestaurantImage
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_address_title
import shareat.feature.restaurant.ui.generated.resources.restaurant_address_unavailable
import shareat.feature.restaurant.ui.generated.resources.restaurant_close
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_allergens
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_allergens_none
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_allergens_unknown
import shareat.feature.restaurant.ui.generated.resources.restaurant_rating_title
import shareat.feature.restaurant.ui.generated.resources.restaurant_reviews_count
import shareat.feature.restaurant.ui.generated.resources.restaurant_unrated

@Composable
internal fun RestaurantPublicSheet(
    header: RestaurantHeaderUiState,
    dish: DishCardUiState?,
    showAddress: Boolean,
    onClose: () -> Unit,
    onRatingClick: (Int) -> Unit,
    onReportReview: (String, ReviewReportReason) -> Unit,
    onBlockReviewer: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.restaurant_close))
        }
        when {
            dish != null -> PublicDishDetails(dish, onRatingClick, onReportReview, onBlockReviewer)
            showAddress -> {
                Text(stringResource(Res.string.restaurant_address_title), style = MaterialTheme.typography.headlineSmall)
                Text(
                    header.address.ifBlank { stringResource(Res.string.restaurant_address_unavailable) },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            else -> {
                Text(stringResource(Res.string.restaurant_rating_title), style = MaterialTheme.typography.headlineSmall)
                PublicRatingSummary(header.ratingLabel, header.reviewCount)
            }
        }
    }
}

@Composable
private fun PublicDishDetails(
    dish: DishCardUiState,
    onRatingClick: (Int) -> Unit,
    onReportReview: (String, ReviewReportReason) -> Unit,
    onBlockReviewer: (String) -> Unit,
) {
    RestaurantDishDetails(
        name = dish.name,
        description = dish.description.orEmpty(),
        priceLabel = dish.priceLabel,
        image = { imageModifier ->
            RestaurantImage(
                imageUrl = dish.imageUrl,
                contentDescription = dish.imageDescription ?: dish.name,
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
            )
        },
        ratingContent = { PublicRatingSummary(dish.ratingLabel, dish.reviewCount) },
        allergenContent = { PublicDishAllergens(dish) },
        actions = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DishRatingBar(selectedRating = dish.selectedRating, onRatingClick = onRatingClick)
                DishReviewsCarousel(
                    reviews = dish.reviews,
                    comments = dish.comments,
                    onReportReview = onReportReview,
                    onBlockReviewer = onBlockReviewer,
                )
            }
        },
    )
}

@Composable
private fun PublicRatingSummary(ratingLabel: String?, reviewCount: Int) {
    ratingLabel?.let { RatingBadge(it) }
        ?: Text(stringResource(Res.string.restaurant_unrated), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
        pluralStringResource(Res.plurals.restaurant_reviews_count, reviewCount, reviewCount.toString()),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicDishAllergens(dish: DishCardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.restaurant_dish_allergens), style = MaterialTheme.typography.titleSmall)
        if (!dish.declaresAllergens || dish.allergens.isEmpty()) {
            Text(
                stringResource(
                    if (dish.declaresAllergens) Res.string.restaurant_dish_allergens_none
                    else Res.string.restaurant_dish_allergens_unknown,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dish.allergens.forEach { allergen ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(allergen.label(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}
