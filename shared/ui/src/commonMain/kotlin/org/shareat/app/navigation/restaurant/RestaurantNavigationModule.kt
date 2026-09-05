package org.shareat.app.navigation.restaurant

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.restaurant.ui.di.restaurantUiModule
import org.shareat.feature.restaurant.ui.navigation.RestaurantKey
import org.shareat.feature.restaurant.ui.navigation.RestaurantNavigation
import org.shareat.feature.restaurant.ui.restaurant.RestaurantScreen

@OptIn(KoinExperimentalAPI::class)
val restaurantNavigationModule = module {
    includes(restaurantUiModule)

    factory<RestaurantNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        RestaurantNavigationImpl(navigator = navigator)
    }

    navigation<RestaurantKey> { key -> RestaurantScreen(args = key.restaurant) }
}
