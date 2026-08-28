package org.shareat.app.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import org.shareat.app.auth.SessionCoordinator
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.feature.login.ui.LoginKey
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingKey
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.shared.navigation.RequiresLogin

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator is not available in the current composition")
}

class Navigator(
    val state: NavigationState,
    private val sessions: SessionCoordinator,
    private val restaurantProfiles: RestaurantProfileCoordinator,
) {
    fun navigate(route: NavKey) {
        if (restaurantProfiles.requiresOnboarding && route != RestaurantOnboardingKey) {
            requireRestaurantOnboarding()
            return
        }
        if (route is RequiresLogin && !sessions.isAuthenticated) {
            state.backStacks[state.topLevelRoute]?.add(LoginKey(route))
            return
        }

        navigateToRoute(route)
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")

        if (currentStack.lastOrNull() == RestaurantOnboardingKey) return
        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

    fun completeLogin() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val loginKey = currentStack.lastOrNull() as? LoginKey
            ?: error("The current route is not a LoginKey")

        currentStack.removeLast()
        loginKey.redirectRoute?.let(::navigate) ?: currentStack.removeLast()
    }

    fun goHome() {
        state.resetToStartRoute()
    }

    fun requireRestaurantOnboarding() {
        state.resetToStartRoute()
        state.topLevelRoute = ProfileKey
        state.backStacks.getValue(ProfileKey).add(RestaurantOnboardingKey)
    }

    fun completeRestaurantOnboarding() {
        restaurantProfiles.completeOnboarding()
        state.resetToStartRoute()
        state.topLevelRoute = ProfileKey
        state.backStacks.getValue(ProfileKey).add(SettingsKey)
    }

    fun isRestaurantOnboardingVisible(): Boolean =
        state.backStacks[ProfileKey]?.lastOrNull() == RestaurantOnboardingKey

    private fun navigateToRoute(route: NavKey) {
        if (route in state.backStacks) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }
}
