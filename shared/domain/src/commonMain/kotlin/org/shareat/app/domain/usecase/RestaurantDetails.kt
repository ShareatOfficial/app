package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.toRatingSummary

data class RestaurantDetails(
    val restaurant: Restaurant,
    val ratingSummary: RatingSummary,
    val dishHighlights: List<DishReviewHighlight>,
    val menu: RestaurantMenu?,
    val isOpen: Boolean,
)

data class DishReviewHighlight(
    val dish: Dish,
    val review: Review,
)

data class RestaurantMenu(
    val menu: Menu,
    val dishes: List<RatedMenuDish>,
)

data class RatedMenuDish(
    val menuDish: MenuDish,
    val reviews: List<Review>,
) {
    val ratingSummary: RatingSummary get() = reviews.toRatingSummary()
}

data class RestaurantSummary(
    val restaurant: Restaurant,
    val ratingSummary: RatingSummary,
    val dishHighlights: List<DishReviewHighlight>,
    val isOpen: Boolean,
)
