package org.shareat.feature.lastactivity.domain

import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.UnlistedDishReview

sealed interface LastActivityItem {
    val id: ReviewId
    val updatedAt: IsoTimestamp

    data class Catalog(val review: Review, val target: ReviewedTarget) : LastActivityItem {
        override val id: ReviewId get() = review.id
        override val updatedAt: IsoTimestamp get() = review.updatedAt
    }

    data class Unlisted(val review: UnlistedDishReview) : LastActivityItem {
        override val id: ReviewId get() = review.id
        override val updatedAt: IsoTimestamp get() = review.updatedAt
    }
}

sealed interface ReviewedTarget {
    data class Dish(val dish: org.shareat.app.domain.model.Dish) : ReviewedTarget
    data class Restaurant(val restaurant: org.shareat.app.domain.model.Restaurant) : ReviewedTarget
}
