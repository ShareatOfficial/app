package org.shareat.feature.home.domain

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review

data class RestaurantWithHighlights(
    val restaurant: Restaurant,
    val ratingSummary: RatingSummary,
    val dishHighlights: List<DishReviewHighlight>,
    val isOpen: Boolean,
)

data class DishReviewHighlight(
    val dish: Dish,
    val review: Review,
)
