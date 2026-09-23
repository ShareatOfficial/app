package org.shareat.app.navigation.login

import org.shareat.app.navigation.Navigator
import org.shareat.feature.login.ui.LoginNavigation

class LoginNavigationImpl(
    private val navigator: Navigator,
) : LoginNavigation {
    override fun goBack() {
        navigator.goBack()
    }

    override fun onLoginSuccess() {
        navigator.completeLogin()
    }
}
