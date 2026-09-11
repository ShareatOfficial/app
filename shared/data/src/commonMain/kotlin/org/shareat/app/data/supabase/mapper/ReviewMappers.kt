package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.RatingSummaryDto
import org.shareat.app.data.supabase.model.ReviewDto
import org.shareat.app.data.supabase.model.SaveReviewRpc
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility

internal fun ReviewDto.toDomain(): Review = Review(
    id = ReviewId(id),
    authorAccountId = AccountId(authorAccountId),
    target = restaurantId?.let { ReviewTarget.Restaurant(RestaurantId(it)) }
        ?: ReviewTarget.Dish(DishId(requireNotNull(dishId))),
    rating = Rating(rating),
    comment = comment,
    visibility = if (visibility == "public") ReviewVisibility.Public else ReviewVisibility.Private,
    moderationStatus = when (moderationStatus) {
        "visible" -> ReviewModerationStatus.Visible
        "hidden" -> ReviewModerationStatus.Hidden
        "removed" -> ReviewModerationStatus.Removed
        else -> error("Unsupported moderation status: $moderationStatus")
    },
    visitedAt = visitedAt?.let(::IsoTimestamp),
    createdAt = IsoTimestamp(createdAt),
    updatedAt = IsoTimestamp(updatedAt),
)

internal fun RatingSummaryDto.toDomain(): RatingSummary = RatingSummary(
    averageTenths = averageTenths,
    ratingCount = ratingCount.toInt(),
)

internal fun ReviewDraft.toSaveRpc(): SaveReviewRpc = SaveReviewRpc(
    targetType = target.toDatabaseValue(),
    restaurantId = (target as? ReviewTarget.Restaurant)?.restaurantId?.value,
    dishId = (target as? ReviewTarget.Dish)?.dishId?.value,
    rating = rating.value,
    comment = comment,
    visibility = visibility.toDatabaseValue(),
    visitedAt = visitedAt?.value,
)

internal fun ReviewTarget.toDatabaseValue(): String = when (this) {
    is ReviewTarget.Restaurant -> "restaurant"
    is ReviewTarget.Dish -> "dish"
}

private fun ReviewVisibility.toDatabaseValue(): String = when (this) {
    ReviewVisibility.Public -> "public"
    ReviewVisibility.Private -> "private"
}
