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
    /** The restaurant's published menu, or null while it has none the public can read. */
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
    /** Every public, visible review of the dish, the source of both its average and its count. */
    val reviews: List<Review>,
) {
    val ratingSummary: RatingSummary get() = reviews.toRatingSummary()
}
