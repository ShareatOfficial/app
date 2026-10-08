package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.SaveUnlistedDishReviewRpc
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft

internal fun UnlistedDishReviewDto.toDomain(imageUrl: String?): UnlistedDishReview = UnlistedDishReview(
    id = ReviewId(id),
    authorAccountId = AccountId(authorAccountId),
    restaurantName = restaurantName,
    dishName = dishName,
    image = imageUrl?.let { ImageRef(it, dishName) },
    rating = Rating(rating),
    comment = comment,
    visibility = when (visibility) {
        "public" -> ReviewVisibility.Public
        "private" -> ReviewVisibility.Private
        else -> error("Unsupported review visibility: $visibility")
    },
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

internal fun UnlistedDishReviewDraft.toSaveRpc(imagePath: String) = SaveUnlistedDishReviewRpc(
    restaurantName = restaurantName,
    dishName = dishName,
    imagePath = imagePath,
    rating = rating.value,
    comment = comment,
    visibility = when (visibility) {
        ReviewVisibility.Public -> "public"
        ReviewVisibility.Private -> "private"
    },
    visitedAt = visitedAt?.value,
)

/** Acknowledges a confirmed RPC; the next read replaces provisional metadata with server values. */
internal fun UnlistedDishReviewDraft.toConfirmedReview(id: String, submittedAt: IsoTimestamp) = UnlistedDishReview(
    id = ReviewId(id),
    authorAccountId = authorAccountId,
    restaurantName = restaurantName,
    dishName = dishName,
    image = null,
    rating = rating,
    comment = comment,
    visibility = visibility,
    moderationStatus = ReviewModerationStatus.Hidden,
    visitedAt = visitedAt,
    createdAt = submittedAt,
    updatedAt = submittedAt,
)
