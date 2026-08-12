package org.shareat.app.navigation.home

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.LocalNavigator
import org.shareat.feature.home.Home
import org.shareat.feature.home.HomeDetails
import org.shareat.feature.home.navigation.HomeDetailsNavigation
import org.shareat.feature.home.navigation.HomeNavigation

@OptIn(KoinExperimentalAPI::class)
val homeNavigationModule = module {
    factory<HomeNavigation> { parameters ->
        HomeNavigationImpl(navigator = parameters.get())
    }
    factory<HomeDetailsNavigation> { parameters ->
        HomeDetailsNavigationImpl(navigator = parameters.get())
    }

    navigation<HomeKey> {
        val appNavigator = LocalNavigator.current
        val homeNavigator = koinInject<HomeNavigation> {
            parametersOf(appNavigator)
        }

        Home(navigator = homeNavigator)
    }

    navigation<HomeDetailsKey> {
        val appNavigator = LocalNavigator.current
        val homeDetailsNavigator = koinInject<HomeDetailsNavigation> {
            parametersOf(appNavigator)
        }

        HomeDetails(navigator = homeDetailsNavigator)
    }
}
