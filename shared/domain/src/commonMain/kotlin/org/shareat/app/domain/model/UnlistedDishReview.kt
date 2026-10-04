package org.shareat.app.domain.model

/** A customer's own dish and restaurant description, independent of the catalogue. */
data class UnlistedDishReview(
    val id: ReviewId,
    val authorAccountId: AccountId,
    val restaurantName: String,
    val dishName: String,
    /** The uploaded photo may temporarily lack a readable URL (for example, while offline). */
    val image: ImageRef?,
    val rating: Rating,
    val comment: String,
    val visibility: ReviewVisibility,
    val moderationStatus: ReviewModerationStatus,
    val visitedAt: IsoTimestamp? = null,
    val createdAt: IsoTimestamp,
    val updatedAt: IsoTimestamp,
) {
    init {
        require(restaurantName.isNotBlank())
        require(dishName.isNotBlank())
        require(comment.isNotBlank())
    }
}

data class UnlistedDishReviewDraft(
    val authorAccountId: AccountId,
    val restaurantName: String,
    val dishName: String,
    val image: ImageUpload,
    val rating: Rating,
    val comment: String,
    val visibility: ReviewVisibility = ReviewVisibility.Public,
    val visitedAt: IsoTimestamp? = null,
) {
    init {
        require(restaurantName.isNotBlank())
        require(dishName.isNotBlank())
        require(comment.isNotBlank())
        require(image.mimeType == "image/jpeg")
    }
}
