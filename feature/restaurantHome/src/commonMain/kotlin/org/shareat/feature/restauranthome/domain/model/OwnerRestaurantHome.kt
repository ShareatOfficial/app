package org.shareat.feature.restauranthome.domain.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review

/** The complete private management view of the authenticated restaurant owner's profile. */
data class OwnerRestaurantHome(
    val restaurant: Restaurant,
    val restaurantRatingSummary: RatingSummary,
    val menu: OwnerRestaurantMenu?,
) {
    /** Categories currently declared by menu items. They are read-only until category persistence exists. */
    val dishCategories: Set<DishCategory> = menu
        ?.dishes
        ?.mapNotNull { it.menuDish.category }
        ?.toSet()
        .orEmpty()
}

/** Unlike the public menu, this representation deliberately includes disabled menu items and dishes. */
data class OwnerRestaurantMenu(
    val menu: Menu,
    val dishes: List<OwnerRatedMenuDish>,
)

data class OwnerRatedMenuDish(
    val menuDish: MenuDish,
    val reviews: List<Review>,
) {
    val ratingSummary: RatingSummary = RatingSummary.of(reviews.map(Review::rating))
}
