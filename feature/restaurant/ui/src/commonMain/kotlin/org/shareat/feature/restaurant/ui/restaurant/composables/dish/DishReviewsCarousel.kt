package org.shareat.feature.restaurant.ui.restaurant.composables.dish

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restaurant.ui.model.DishReviewUiState
import org.shareat.shared.designsystem.components.ReviewCard
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_reviews_empty
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_reviews_no_comments

@Composable
internal fun DishReviewsCarousel(
    reviews: List<DishReviewUiState>,
    comments: List<DishReviewUiState>,
    modifier: Modifier = Modifier,
) {
    if (comments.isEmpty()) {
        Text(
            text = stringResource(
                if (reviews.isEmpty()) {
                    Res.string.restaurant_dish_reviews_empty
                } else {
                    Res.string.restaurant_dish_reviews_no_comments
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.fillMaxWidth().padding(bottom = 4.dp),
        )
        return
    }
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        comments.forEach { review ->
            ReviewCard(comment = review.comment.orEmpty(), rating = review.rating)
        }
    }
}

@Preview
@Composable
private fun DishReviewsCarouselPreview() {
    ShareatTheme {
        val comments = listOf(
            DishReviewUiState("review-1", rating = 5, comment = "La mejor pizza del barrio."),
            DishReviewUiState("review-2", rating = 3, comment = "Correcta, sin más."),
        )
        DishReviewsCarousel(reviews = comments, comments = comments)
    }
}

@Preview
@Composable
private fun DishReviewsCarouselEmptyPreview() {
    ShareatTheme {
        DishReviewsCarousel(reviews = emptyList(), comments = emptyList())
    }
}
