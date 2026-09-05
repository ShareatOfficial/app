package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restaurant.ui.model.RestaurantHeaderUiState
import org.shareat.shared.designsystem.shimmerEffect
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_leave_your_rate
import shareat.feature.restaurant.ui.generated.resources.restaurant_reviews_count
import shareat.feature.restaurant.ui.generated.resources.restaurant_unrated
import shareat.feature.restaurant.ui.generated.resources.restaurant_verified

private val HeroHeight = 180.dp
private val CardShape = RoundedCornerShape(20.dp)
private const val DescriptionMaxLines = 3

@Composable
internal fun RestaurantInfoCard(
    header: RestaurantHeaderUiState,
    onLeaveRateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = CardShape) {
        Column {
            RestaurantHero(header = header)
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RestaurantName(name = header.name, isVerified = header.isVerified)
                header.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = DescriptionMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    RestaurantRating(
                        ratingLabel = header.ratingLabel,
                        reviewCount = header.reviewCount,
                    )
                    Button(onClick = onLeaveRateClick) {
                        Text(text = stringResource(Res.string.restaurant_leave_your_rate))
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantHero(header: RestaurantHeaderUiState) {
    Box(modifier = Modifier.fillMaxWidth().height(HeroHeight)) {
        AsyncImage(
            model = header.heroImageUrl,
            contentDescription = header.heroImageDescription,
            modifier = Modifier.fillMaxWidth().height(HeroHeight),
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            header.cuisineLabel?.let { HeroChip(text = it) }
            header.priceRangeLabel?.let { HeroChip(text = it) }
            if (header.cuisineLabel == null && header.priceRangeLabel == null) {
                HeroChip(text = header.address)
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun RestaurantName(name: String, isVerified: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (isVerified) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = stringResource(Res.string.restaurant_verified),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun RestaurantRating(ratingLabel: String?, reviewCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (ratingLabel == null) {
            Text(
                text = stringResource(Res.string.restaurant_unrated),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            StarRating(
                ratingLabel = ratingLabel,
                textStyle = MaterialTheme.typography.headlineSmall,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = pluralStringResource(
                    Res.plurals.restaurant_reviews_count,
                    reviewCount,
                    reviewCount.toString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun RestaurantInfoCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColor = MaterialTheme.colorScheme.onSurface
    Card(modifier = modifier.fillMaxWidth(), shape = CardShape) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(HeroHeight).shimmerEffect(shimmerColor))
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShimmerBar(widthFraction = 0.6f, height = 24.dp)
                ShimmerBar(widthFraction = 1f, height = 12.dp)
                ShimmerBar(widthFraction = 0.8f, height = 12.dp)
                ShimmerBar(widthFraction = 0.3f, height = 20.dp)
            }
        }
    }
}

@Preview
@Composable
private fun RestaurantInfoCardPreview() {
    ShareatTheme {
        RestaurantInfoCard(
            header = RestaurantHeaderUiState(
                name = "The Rustic Spoon",
                address = "Calle del Olmo, 18, Madrid",
                description = "Texto descriptivo del restaurante con producto local y cocina de temporada.",
                cuisineLabel = "Modern European",
                priceRangeLabel = "$$ · Moderate",
                isVerified = true,
                ratingLabel = "4,8",
                reviewCount = 1_284,
            ),
            onLeaveRateClick = {},
        )
    }
}

@Preview
@Composable
private fun RestaurantInfoCardUnratedPreview() {
    ShareatTheme {
        RestaurantInfoCard(
            header = RestaurantHeaderUiState(
                name = "Casa Naranja",
                address = "Calle del Olmo, 18, Madrid",
            ),
            onLeaveRateClick = {},
        )
    }
}

@Preview
@Composable
private fun RestaurantInfoCardSkeletonPreview() {
    ShareatTheme {
        RestaurantInfoCardSkeleton()
    }
}
