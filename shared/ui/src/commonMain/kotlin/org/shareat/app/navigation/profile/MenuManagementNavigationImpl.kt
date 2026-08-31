package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.menu.ui.MenuManagementNavigation

class MenuManagementNavigationImpl(
    private val navigator: Navigator,
) : MenuManagementNavigation {
    override fun goBack() = navigator.goBack()
}
