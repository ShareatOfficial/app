package org.shareat.app.navigation.restauranthome

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.feature.restauranthome.ui.di.restaurantHomeUiModule
import org.shareat.feature.restauranthome.ui.navigation.RestaurantHomeKey
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomeScreen

@OptIn(KoinExperimentalAPI::class)
val restaurantHomeNavigationModule = module {
    includes(restaurantHomeUiModule)

    navigation<RestaurantHomeKey> { RestaurantHomeScreen() }
}
