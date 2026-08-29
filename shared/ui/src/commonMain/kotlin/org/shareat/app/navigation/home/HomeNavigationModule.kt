package org.shareat.app.navigation.home

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.feature.home.ui.di.homeUiModule
import org.shareat.feature.home.ui.home.HomeScreen
import org.shareat.feature.home.ui.navigation.HomeKey

@OptIn(KoinExperimentalAPI::class)
val homeNavigationModule = module {
    includes(homeUiModule)

    navigation<HomeKey> { HomeScreen() }
}
