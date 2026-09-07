package org.shareat.app.navigation.home

import org.shareat.app.domain.usecase.RestaurantSummary
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.ui.navigation.HomeNavigation
import org.shareat.feature.restaurant.ui.model.toArgs
import org.shareat.feature.restaurant.ui.navigation.RestaurantKey

class HomeNavigationImpl(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openRestaurant(restaurant: RestaurantSummary) {
        navigator.navigate(RestaurantKey(restaurant.toArgs()))
    }
}
