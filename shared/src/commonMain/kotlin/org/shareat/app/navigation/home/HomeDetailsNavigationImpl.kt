package org.shareat.app.navigation.home

import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.navigation.HomeDetailsNavigation

class HomeDetailsNavigationImpl(
    private val navigator: Navigator,
) : HomeDetailsNavigation {
    override fun goBack() {
        navigator.goBack()
    }
}
