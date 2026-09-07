package org.shareat.feature.home.ui.navigation

import org.shareat.app.domain.usecase.RestaurantSummary

interface HomeNavigation {
    fun openRestaurant(restaurant: RestaurantSummary)
}
