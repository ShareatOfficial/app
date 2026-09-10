package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation

/**
 * Compatibility binding for the legacy form while it remains in the settings module.
 *
 * The form is no longer registered as a route and no login flow can reach it; if an old restored
 * entry invokes this callback, returning home lets the profile coordinator select the current
 * role-based landing again.
 */
class RestaurantOnboardingNavigationImpl(
    private val navigator: Navigator,
) : RestaurantOnboardingNavigation {
    override fun onCompleted() = navigator.goHome()

    override fun onLogoutSuccess() = navigator.goHome()
}
