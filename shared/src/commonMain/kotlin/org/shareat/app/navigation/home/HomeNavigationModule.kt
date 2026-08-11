package org.shareat.app.navigation.home

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.LocalNavigator
import org.shareat.feature.home.Home
import org.shareat.feature.home.HomeDetails

@OptIn(KoinExperimentalAPI::class)
val homeNavigationModule = module {
    navigation<HomeKey> {
        val navigator = LocalNavigator.current
        Home(onOpenDetails = { navigator.navigate(HomeDetailsKey) })
    }
    navigation<HomeDetailsKey> {
        val navigator = LocalNavigator.current
        HomeDetails(onBack = navigator::goBack)
    }
}
