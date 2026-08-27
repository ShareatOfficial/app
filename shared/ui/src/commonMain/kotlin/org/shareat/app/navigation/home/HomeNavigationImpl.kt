package org.shareat.app.navigation.home

import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.navigation.HomeDetailsKey
import org.shareat.feature.home.navigation.HomeNavigation

class HomeNavigationImpl(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openHomeDetails() {
        navigator.navigate(HomeDetailsKey)
    }
}
