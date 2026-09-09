package org.shareat.app.navigation.lastactivity

import org.shareat.app.navigation.Navigator
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.lastactivity.ui.LastActivityNavigation
import org.shareat.feature.login.ui.LoginKey

class LastActivityNavigationImpl(
    private val navigator: Navigator,
) : LastActivityNavigation {
    override fun openLogin() {
        navigator.navigate(LoginKey(LastActivityKey))
    }
}
