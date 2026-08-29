package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.feature.profile.ui.settings.SettingsKey

class RestaurantOnboardingNavigationImpl(
    private val navigator: Navigator,
    private val restaurantProfiles: RestaurantProfileCoordinator,
) : RestaurantOnboardingNavigation {
    override fun onCompleted() {
        restaurantProfiles.completeOnboarding()
        navigator.goHome()
        navigator.navigate(ProfileKey)
        navigator.navigate(SettingsKey)
    }
    override fun onLogoutSuccess() = navigator.goHome()
}
