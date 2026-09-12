package org.shareat.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.shareat.app.navigation.LocalNavigator
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.ui.navigation.HomeKey
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.app.navigation.rememberNavigationState
import org.shareat.app.navigation.toEntries
import org.shareat.app.navscenedecorator.TopLevelNavigationBar
import org.shareat.app.navscenedecorator.TopLevelNavigationRail
import org.shareat.app.navscenedecorator.rememberRestaurantProfileGateSceneDecoratorStrategy
import org.shareat.app.navscenedecorator.rememberResponsiveNavigationSceneDecoratorStrategy
import org.shareat.app.navscenedecorator.topLevelNavigationItems
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.app.auth.RestaurantProfileGateState
import org.shareat.app.domain.model.AccountRole
import org.shareat.feature.restauranthome.ui.navigation.RestaurantHomeKey
import org.shareat.shared.designsystem.theme.ShareatTheme

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    ShareatTheme {
        SharedTransitionLayout {
            val restaurantProfiles = koinInject<RestaurantProfileCoordinator>()
            val profileGateState by restaurantProfiles.state.collectAsState()
            val topLevelRoutes = remember {
                setOf<NavKey>(HomeKey, RestaurantHomeKey, LastActivityKey, SettingsKey)
            }
            val navigationState = rememberNavigationState(
                startRoute = HomeKey,
                topLevelRoutes = topLevelRoutes,
            )
            val landingRoute = (profileGateState as? RestaurantProfileGateState.Allowed)
                ?.let { allowed ->
                    if (allowed.role == AccountRole.Restaurant) RestaurantHomeKey else HomeKey
                }
            var appliedLandingRoute by remember(navigationState) { mutableStateOf<NavKey?>(null) }
            val displayedGateState = if (landingRoute != null && landingRoute != appliedLandingRoute) {
                RestaurantProfileGateState.Checking
            } else {
                profileGateState
            }
            LaunchedEffect(landingRoute) {
                landingRoute?.let { route ->
                    if (route != appliedLandingRoute) {
                        navigationState.resetToLandingRoute(route)
                        appliedLandingRoute = route
                    }
                }
            }
            val navigator = koinInject<Navigator> {
                parametersOf(navigationState)
            }

            val koin = getKoin()
            remember(navigator) { koin.declare(navigator) }

            val topLevelItems = topLevelNavigationItems(landingRoute ?: HomeKey)
            val navigationSceneDecorator =
                rememberResponsiveNavigationSceneDecoratorStrategy<NavKey>(
                    navBar = { TopLevelNavigationBar(topLevelItems, navigator) },
                    navRail = { TopLevelNavigationRail(topLevelItems, navigator) },
                    sharedTransitionScope = this,
                )
            val restaurantProfileGateDecorator =
                rememberRestaurantProfileGateSceneDecoratorStrategy<NavKey>(
                    restaurantProfiles = restaurantProfiles,
                    gateStateOverride = displayedGateState,
                )
            val entryProvider = koinEntryProvider<NavKey>()

            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    sceneDecoratorStrategies = listOf(
                        navigationSceneDecorator,
                        restaurantProfileGateDecorator,
                    ),
                    sharedTransitionScope = this,
                    onBack = navigator::goBack,
                )
            }
        }
    }
}
