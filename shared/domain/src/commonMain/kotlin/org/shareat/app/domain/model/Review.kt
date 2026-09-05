package org.shareat.app.domain.model

import kotlin.jvm.JvmInline

sealed interface ReviewTarget {
    data class Restaurant(val restaurantId: RestaurantId) : ReviewTarget
    data class Dish(val dishId: DishId) : ReviewTarget
}

@JvmInline
value class Rating(val value: Int) {
    init { require(value in 1..5) }
}

enum class ReviewVisibility {
    Public,
    Private,
}

enum class ReviewModerationStatus {
    Visible,
    Hidden,
    Removed,
}

data class Review(
    val id: ReviewId,
    val authorAccountId: AccountId,
    val target: ReviewTarget,
    val rating: Rating,
    val comment: String? = null,
    val visibility: ReviewVisibility,
    val moderationStatus: ReviewModerationStatus,
    val visitedAt: IsoTimestamp? = null,
    val createdAt: IsoTimestamp,
    val updatedAt: IsoTimestamp,
) {
    init { require(comment == null || comment.isNotBlank()) }
}

data class ReviewDraft(
    val authorAccountId: AccountId,
    val target: ReviewTarget,
    val rating: Rating,
    val comment: String? = null,
    val visibility: ReviewVisibility,
    val visitedAt: IsoTimestamp? = null,
) {
    init { require(comment == null || comment.isNotBlank()) }
}

data class RatingSummary(
    /** Average multiplied by ten, e.g. 48 means 4.8. Null when there are no ratings. */
    val averageTenths: Int?,
    val ratingCount: Int,
) {
    init {
        require(ratingCount >= 0)
        require(averageTenths == null || averageTenths in 10..50)
        require((ratingCount == 0) == (averageTenths == null))
    }

    companion object {
        val Unrated = RatingSummary(averageTenths = null, ratingCount = 0)

        fun of(ratings: List<Rating>): RatingSummary {
            if (ratings.isEmpty()) return Unrated
            val values = ratings.map(Rating::value)
            return RatingSummary(
                averageTenths = (values.sum() * 10 + values.size / 2) / values.size,
                ratingCount = values.size,
            )
        }
    }
}

fun List<Review>.toRatingSummary(): RatingSummary = RatingSummary.of(map(Review::rating))
