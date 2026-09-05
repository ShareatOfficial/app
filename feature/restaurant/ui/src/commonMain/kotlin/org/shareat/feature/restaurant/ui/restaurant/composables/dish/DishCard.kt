package org.shareat.feature.restaurant.ui.restaurant.composables.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.pluralStringResource
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restaurant.ui.model.DishCardUiState
import org.shareat.feature.restaurant.ui.model.DishReviewUiState
import org.shareat.feature.restaurant.ui.restaurant.composables.ShimmerBar
import org.shareat.feature.restaurant.ui.restaurant.composables.StarRating
import org.shareat.feature.restaurant.ui.restaurant.composables.label
import org.shareat.shared.designsystem.shimmerEffect
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_reviews_count

private val CardShape = RoundedCornerShape(16.dp)
private val ThumbnailSize = 84.dp
private const val CollapsedDescriptionLines = 2

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DishCard(
    dish: DishCardUiState,
    onClick: () -> Unit,
    onRatingClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = CardShape,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DishThumbnail(imageUrl = dish.imageUrl, description = dish.name)
                Column(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DishNameAndPrice(name = dish.name, priceLabel = dish.priceLabel)
                    dish.ratingLabel?.let { rating ->
                        StarRating(
                            ratingLabel = rating,
                            tint = MaterialTheme.colorScheme.tertiary,
                            trailingText = pluralStringResource(
                                Res.plurals.restaurant_reviews_count,
                                dish.reviewCount,
                                dish.reviewCount.toString(),
                            ),
                        )
                    }
                    dish.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (dish.isExpanded) {
                                Int.MAX_VALUE
                            } else {
                                CollapsedDescriptionLines
                            },
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (dish.allergens.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            dish.allergens.forEach { AllergenTag(allergen = it) }
                        }
                    }
                }
            }
            DishExpandedContent(
                isExpanded = dish.isExpanded,
                selectedRating = dish.selectedRating,
                reviews = dish.reviews,
                comments = dish.comments,
                onRatingClick = onRatingClick,
            )
        }
    }
}

@Composable
private fun DishNameAndPrice(name: String, priceLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = priceLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DishThumbnail(imageUrl: String?, description: String) {
    Box(
        modifier = Modifier
            .size(ThumbnailSize)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(ThumbnailSize),
            )
        }
    }
}

@Composable
private fun AllergenTag(allergen: EuAllergen) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline),
        )
        Text(
            text = allergen.label(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

@Composable
internal fun DishCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColor = MaterialTheme.colorScheme.onSurface
    Card(modifier = modifier.fillMaxWidth(), shape = CardShape) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(ThumbnailSize)
                    .shimmerEffect(shimmerColor, RoundedCornerShape(12.dp)),
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ShimmerBar(widthFraction = 0.45f, height = 14.dp)
                    Box(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(14.dp)
                            .shimmerEffect(shimmerColor, RoundedCornerShape(4.dp)),
                    )
                }
                ShimmerBar(widthFraction = 0.3f, height = 12.dp)
                ShimmerBar(widthFraction = 0.85f, height = 12.dp)
            }
        }
    }
}

private val previewDish = DishCardUiState(
    id = "dish-1",
    name = "Margherita Verace",
    priceLabel = "18€",
    description = "San Marzano tomato sauce, fresh mozzarella di bufala, albahaca fresca.",
    reviews = listOf(
        DishReviewUiState("review-1", rating = 5, comment = "La mejor pizza del barrio."),
        DishReviewUiState("review-2", rating = 4, comment = "Masa perfecta, un poco justa de sal."),
        DishReviewUiState("review-3", rating = 4),
    ),
    allergens = listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Fish, EuAllergen.Soybeans),
)

@Preview
@Composable
private fun DishCardPreview() {
    ShareatTheme {
        DishCard(dish = previewDish, onClick = {}, onRatingClick = {})
    }
}

@Preview
@Composable
private fun DishCardExpandedPreview() {
    ShareatTheme {
        DishCard(
            dish = previewDish.copy(isExpanded = true, selectedRating = 4),
            onClick = {},
            onRatingClick = {},
        )
    }
}

@Preview
@Composable
private fun DishCardExpandedWithoutReviewsPreview() {
    ShareatTheme {
        DishCard(
            dish = previewDish.copy(isExpanded = true, reviews = emptyList()),
            onClick = {},
            onRatingClick = {},
        )
    }
}

@Preview
@Composable
private fun DishCardSkeletonPreview() {
    ShareatTheme {
        DishCardSkeleton()
    }
}
