package org.shareat.feature.restaurant.ui.restaurant.composables.dish

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restaurant.ui.model.DishReviewUiState
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.shared.designsystem.components.ReviewCard
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_reviews_empty
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_reviews_no_comments
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_block
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_block_explanation
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_block_title
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_cancel
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report_offensive
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report_other
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report_spam
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report_title

@Composable
internal fun DishReviewsCarousel(
    reviews: List<DishReviewUiState>,
    comments: List<DishReviewUiState>,
    modifier: Modifier = Modifier,
    onReportReview: (String, ReviewReportReason) -> Unit = { _, _ -> },
    onBlockReviewer: (String) -> Unit = {},
) {
    var reportReviewId by remember { mutableStateOf<String?>(null) }
    var blockReviewId by remember { mutableStateOf<String?>(null) }
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
            Column {
                ReviewCard(comment = review.comment.orEmpty(), rating = review.rating)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { reportReviewId = review.id }) {
                        Text(stringResource(Res.string.restaurant_review_report))
                    }
                    TextButton(onClick = { blockReviewId = review.id }) {
                        Text(stringResource(Res.string.restaurant_review_block))
                    }
                }
            }
        }
    }

    reportReviewId?.let { reviewId ->
        AlertDialog(
            onDismissRequest = { reportReviewId = null },
            title = { Text(stringResource(Res.string.restaurant_review_report_title)) },
            text = {
                Column {
                    listOf(
                        ReviewReportReason.Offensive to Res.string.restaurant_review_report_offensive,
                        ReviewReportReason.Spam to Res.string.restaurant_review_report_spam,
                        ReviewReportReason.Other to Res.string.restaurant_review_report_other,
                    ).forEach { (reason, label) ->
                        TextButton(onClick = {
                            reportReviewId = null
                            onReportReview(reviewId, reason)
                        }) { Text(stringResource(label)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { reportReviewId = null }) {
                    Text(stringResource(Res.string.restaurant_review_cancel))
                }
            },
        )
    }
    blockReviewId?.let { reviewId ->
        AlertDialog(
            onDismissRequest = { blockReviewId = null },
            title = { Text(stringResource(Res.string.restaurant_review_block_title)) },
            text = { Text(stringResource(Res.string.restaurant_review_block_explanation)) },
            confirmButton = {
                TextButton(onClick = {
                    blockReviewId = null
                    onBlockReviewer(reviewId)
                }) { Text(stringResource(Res.string.restaurant_review_block)) }
            },
            dismissButton = {
                TextButton(onClick = { blockReviewId = null }) {
                    Text(stringResource(Res.string.restaurant_review_cancel))
                }
            },
        )
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
