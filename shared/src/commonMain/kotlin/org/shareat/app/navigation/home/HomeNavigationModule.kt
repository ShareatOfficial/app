package org.shareat.app.navigation.home

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.Home
import org.shareat.feature.home.HomeDetails
import org.shareat.feature.home.navigation.HomeDetailsNavigation
import org.shareat.feature.home.navigation.HomeNavigation

@OptIn(KoinExperimentalAPI::class)
val homeNavigationModule = module {
    factory<HomeNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        HomeNavigationImpl(navigator = navigator)
    }
    factory<HomeDetailsNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        HomeDetailsNavigationImpl(navigator = navigator)
    }

    navigation<HomeKey> { Home() }
    navigation<HomeDetailsKey> { HomeDetails() }
}
