package org.shareat.app.navigation.menu

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.menu.ui.MenuManagementKey
import org.shareat.feature.menu.ui.MenuManagementNavigation
import org.shareat.feature.menu.ui.MenuManagementScreen
import org.shareat.feature.menu.ui.di.menuUiModule

@OptIn(KoinExperimentalAPI::class)
val menuNavigationModule = module {
    includes(menuUiModule)

    factory<MenuManagementNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        MenuManagementNavigationImpl(navigator)
    }

    navigation<MenuManagementKey> { MenuManagementScreen() }
}

private class MenuManagementNavigationImpl(
    private val navigator: Navigator,
) : MenuManagementNavigation {
    override fun goBack() = navigator.goBack()
}
