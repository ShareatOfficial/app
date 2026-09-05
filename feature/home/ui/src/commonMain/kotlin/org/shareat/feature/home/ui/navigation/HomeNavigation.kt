package org.shareat.feature.home.ui.navigation

import org.shareat.app.domain.usecase.RestaurantDetails

interface HomeNavigation {
    fun openRestaurant(restaurant: RestaurantDetails)
}
