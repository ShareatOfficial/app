package org.shareat.feature.review

import org.shareat.app.domain.model.ImageUpload

public data class UnlistedDishReviewUiState(
    val openingToken: Any? = null,
    val restaurantName: String = "",
    val dishName: String = "",
    val image: ImageUpload? = null,
    val rating: Int = 0,
    val comment: String = "",
    val isPreparingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSucceeded: Boolean = false,
    val error: UnlistedDishReviewError? = null,
    val imageError: UnlistedDishReviewImageError? = null,
) {
    public companion object {
        public const val MAX_RESTAURANT_NAME_LENGTH: Int = 120
        public const val MAX_DISH_NAME_LENGTH: Int = 120
        public const val MAX_COMMENT_LENGTH: Int = 2000
    }

    public val canSubmit: Boolean
        get() = !isSubmitting && !isPreparingImage && !submitSucceeded &&
            restaurantName.isNotBlank() && restaurantName.length <= MAX_RESTAURANT_NAME_LENGTH &&
            dishName.isNotBlank() && dishName.length <= MAX_DISH_NAME_LENGTH && image != null &&
            rating in 1..5 && comment.isNotBlank() && comment.length <= MAX_COMMENT_LENGTH
}

public enum class UnlistedDishReviewImageError { UNSUPPORTED_FORMAT, TOO_LARGE, INVALID_FILE }
