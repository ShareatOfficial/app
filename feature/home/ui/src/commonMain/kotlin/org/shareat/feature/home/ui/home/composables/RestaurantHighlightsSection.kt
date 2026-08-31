package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.app.domain.model.RestaurantId
import org.shareat.feature.home.ui.home.model.RestaurantCardUiState
import org.shareat.shared.designsystem.theme.ShareatTheme

private const val RestaurantGridColumns = 2
private const val HighlightsSkeletonCount = 4
private val HighlightsSectionShape = RoundedCornerShape(24.dp)

@Composable
internal fun RestaurantHighlightsSection(
    restaurants: List<RestaurantCardUiState>,
    modifier: Modifier = Modifier,
) {
    HighlightsContainer(modifier = modifier) {
        restaurants.chunked(RestaurantGridColumns).forEach { rowRestaurants ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                rowRestaurants.forEach { restaurant ->
                    RestaurantCard(
                        name = restaurant.name,
                        heroImageUrl = restaurant.heroImageUrl,
                        heroImageDescription = restaurant.heroImageDescription,
                        ratingLabel = restaurant.ratingLabel,
                        isOpen = restaurant.isOpen,
                        address = restaurant.address,
                        dishReviews = restaurant.dishReviews,
                        showDishReviews = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RestaurantHighlightsSectionSkeleton(modifier: Modifier = Modifier) {
    HighlightsContainer(modifier = modifier) {
        (0 until HighlightsSkeletonCount).chunked(RestaurantGridColumns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { RestaurantCardSkeleton(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun HighlightsContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(HighlightsSectionShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Preview
@Composable
private fun RestaurantHighlightsSectionPreview() {
    ShareatTheme {
        RestaurantHighlightsSection(
            restaurants = (0 until 4).map { index ->
                RestaurantCardUiState(
                    id = RestaurantId("restaurant-$index"),
                    name = "Casa Naranja ${index + 1}",
                    heroImageUrl = null,
                    heroImageDescription = null,
                    ratingLabel = "4.8",
                    isOpen = true,
                    address = "Calle del Olmo, 18, Madrid",
                    dishReviews = emptyList(),
                )
            },
        )
    }
}

@Preview
@Composable
private fun RestaurantHighlightsSectionSkeletonPreview() {
    ShareatTheme {
        RestaurantHighlightsSectionSkeleton()
    }
}
