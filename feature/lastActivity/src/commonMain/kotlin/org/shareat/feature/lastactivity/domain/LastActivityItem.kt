package org.shareat.feature.lastactivity.domain

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review

data class LastActivityItem(
    val review: Review,
    val target: ReviewedTarget,
)

sealed interface ReviewedTarget {
    data class Dish(val dish: org.shareat.app.domain.model.Dish) : ReviewedTarget
    data class Restaurant(val restaurant: org.shareat.app.domain.model.Restaurant) : ReviewedTarget
}
