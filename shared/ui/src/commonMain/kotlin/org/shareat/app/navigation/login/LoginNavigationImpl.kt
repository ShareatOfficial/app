package org.shareat.app.navigation.login

import org.shareat.app.navigation.Navigator
import org.shareat.feature.login.ui.LoginNavigation
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingKey

class LoginNavigationImpl(
    private val navigator: Navigator,
) : LoginNavigation {
    override fun onLoginSuccess() {
        navigator.completeLogin()
    }

    override fun onRestaurantRegistrationSuccess() {
        navigator.completeLogin()
        navigator.navigate(RestaurantOnboardingKey)
    }
}
