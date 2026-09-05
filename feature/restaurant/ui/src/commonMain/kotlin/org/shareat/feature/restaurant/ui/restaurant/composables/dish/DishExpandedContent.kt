package org.shareat.feature.restaurant.ui.restaurant.composables.dish

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.restaurant.ui.model.DishReviewUiState
import org.shareat.shared.designsystem.theme.ShareatTheme

private val SectionSpacing = 12.dp

@Composable
internal fun DishExpandedContent(
    isExpanded: Boolean,
    selectedRating: Int?,
    reviews: List<DishReviewUiState>,
    comments: List<DishReviewUiState>,
    onRatingClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = isExpanded, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing),
        ) {
            HorizontalDivider()
            DishRatingBar(selectedRating = selectedRating, onRatingClick = onRatingClick)
            DishReviewsCarousel(reviews = reviews, comments = comments)
        }
    }
}

@Preview
@Composable
private fun DishExpandedContentPreview() {
    ShareatTheme {
        val comments = listOf(
            DishReviewUiState("review-1", rating = 5, comment = "La mejor pizza del barrio."),
            DishReviewUiState("review-2", rating = 3, comment = "Correcta, sin más."),
        )
        DishExpandedContent(
            isExpanded = true,
            selectedRating = 4,
            reviews = comments,
            comments = comments,
            onRatingClick = {},
        )
    }
}
