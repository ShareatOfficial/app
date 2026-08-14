package org.shareat.app.navigation.login

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.app.navscenedecorator.HIDE_NAVIGATION_METADATA
import org.shareat.feature.login.LoginNavigation
import org.shareat.feature.login.LoginScreen

@OptIn(KoinExperimentalAPI::class)
val loginNavigationModule = module {
    factory<LoginNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        LoginNavigationImpl(navigator = navigator)
    }

    navigation<LoginKey>(
        metadata = mapOf(HIDE_NAVIGATION_METADATA to true),
    ) { LoginScreen() }
}
