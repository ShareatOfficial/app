package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation

class RestaurantOnboardingNavigationImpl(
    private val navigator: Navigator,
) : RestaurantOnboardingNavigation {
    override fun onCompleted() = navigator.completeRestaurantOnboarding()
    override fun onLogoutSuccess() = navigator.goHome()
}
