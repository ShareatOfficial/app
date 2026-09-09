package org.shareat.feature.review

internal val DishReviewRatingRange: IntRange = 1..5

public data class DishReviewUiState(
    val dishRating: Int = 0,
    val dishComment: String = "",
    val restaurantRating: Int = 0,
    val restaurantComment: String = "",
    val isSubmitting: Boolean = false,
    val submitSucceeded: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            !submitSucceeded &&
            dishRating in DishReviewRatingRange &&
            restaurantRating in DishReviewRatingRange
}
