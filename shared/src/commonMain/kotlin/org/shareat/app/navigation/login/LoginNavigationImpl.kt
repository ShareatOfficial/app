package org.shareat.app.navigation.login

import org.shareat.app.navigation.Navigator
import org.shareat.feature.login.LoginNavigation

class LoginNavigationImpl(
    private val navigator: Navigator,
) : LoginNavigation {
    override fun onLoginSuccess() {
        navigator.completeLogin()
    }
}
