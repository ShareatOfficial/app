package org.shareat.feature.review.domain.model

import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.ReviewVisibility

data class SubmitUnlistedDishReviewParams(
    val restaurantName: String,
    val dishName: String,
    val image: ImageUpload,
    val rating: Int,
    val comment: String,
    val visibility: ReviewVisibility = ReviewVisibility.Public,
    val visitedAt: IsoTimestamp? = null,
)
