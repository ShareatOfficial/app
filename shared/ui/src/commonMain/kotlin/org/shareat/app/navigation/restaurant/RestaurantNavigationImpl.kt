package org.shareat.app.navigation.restaurant

import org.shareat.app.navigation.Navigator
import org.shareat.feature.restaurant.ui.navigation.RestaurantNavigation

class RestaurantNavigationImpl(
    private val navigator: Navigator,
) : RestaurantNavigation {
    override fun goBack() {
        navigator.goBack()
    }

    override fun openRestaurantReviewForm(restaurantId: String) = Unit
}
