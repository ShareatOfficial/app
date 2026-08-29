package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.shareat.feature.home.ui.home.model.DishReviewUiState

private val RestaurantHeroHeight = 180.dp
private const val RestaurantHeroOverlayHeightFraction = 0.75f

@Composable
internal fun RestaurantCard(
    name: String,
    heroImageUrl: String?,
    heroImageDescription: String?,
    ratingLabel: String,
    isOpen: Boolean,
    address: String,
    dishReviews: List<DishReviewUiState>,
    showDishReviews: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column {
            RestaurantHeroImage(
                heroImageUrl = heroImageUrl,
                heroImageDescription = heroImageDescription,
                name = name,
                ratingLabel = ratingLabel,
                isOpen = isOpen,
                address = address,
            )
            if (showDishReviews && dishReviews.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dishReviews.forEach { dishReview ->
                        DishReviewCard(
                            dishName = dishReview.dishName,
                            comment = dishReview.comment,
                            rating = dishReview.rating,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantHeroImage(
    heroImageUrl: String?,
    heroImageDescription: String?,
    name: String,
    ratingLabel: String,
    isOpen: Boolean,
    address: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RestaurantHeroHeight),
    ) {
        AsyncImage(
            model = heroImageUrl,
            contentDescription = heroImageDescription,
            modifier = Modifier.fillMaxWidth().height(RestaurantHeroHeight),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(RestaurantHeroOverlayHeightFraction)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    ),
                ),
        ) {
            RestaurantInfo(
                name = name,
                ratingLabel = ratingLabel,
                isOpen = isOpen,
                address = address,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun RestaurantInfo(
    name: String,
    ratingLabel: String,
    isOpen: Boolean,
    address: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            RatingBadge(ratingLabel = ratingLabel, contentColor = Color.White)
        }
        OpenStatusBadge(isOpen = isOpen, contentColor = Color.White)
        AddressBadge(address = address, contentColor = Color.White)
    }
}

@Preview
@Composable
private fun RestaurantCardPreview() {
    MaterialTheme {
        RestaurantCard(
            name = "Casa Naranja",
            heroImageUrl = null,
            heroImageDescription = null,
            ratingLabel = "4.8",
            isOpen = true,
            address = "Calle del Olmo, 18, Madrid",
            dishReviews = listOf(
                DishReviewUiState(
                    dishName = "Pulpo a la brasa",
                    comment = "Tiernísimo y con el punto justo de humo.",
                    rating = 5,
                ),
                DishReviewUiState(
                    dishName = "Croquetas de jamón",
                    comment = "Cremosas y recién hechas.",
                    rating = 4,
                ),
            ),
            showDishReviews = true,
        )
    }
}

@Preview
@Composable
private fun RestaurantCardWithoutReviewsPreview() {
    MaterialTheme {
        RestaurantCard(
            name = "Casa Naranja",
            heroImageUrl = null,
            heroImageDescription = null,
            ratingLabel = "New",
            isOpen = false,
            address = "Calle del Olmo, 18, Madrid",
            dishReviews = emptyList(),
            showDishReviews = false,
        )
    }
}
