package org.shareat.app.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import org.shareat.app.auth.SessionCoordinator
import org.shareat.feature.login.ui.LoginKey
import org.shareat.shared.navigation.RequiresLogin

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator is not available in the current composition")
}

class Navigator(
    val state: NavigationState,
    private val sessions: SessionCoordinator,
) {
    fun navigate(route: NavKey) {
        if (route is RequiresLogin && !sessions.isAuthenticated) {
            state.backStacks[state.topLevelRoute]?.add(LoginKey(route))
            return
        }

        navigateToRoute(route)
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")

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

    private fun navigateToRoute(route: NavKey) {
        if (route in state.backStacks) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }
}
