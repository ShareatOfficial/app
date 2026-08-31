package org.shareat.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.app.navigation.rememberNavigationState
import org.shareat.app.navigation.toEntries
import org.shareat.app.navscenedecorator.TOP_LEVEL_NAV_ITEMS
import org.shareat.app.navscenedecorator.TopLevelNavigationBar
import org.shareat.app.navscenedecorator.TopLevelNavigationRail
import org.shareat.app.navscenedecorator.rememberRestaurantProfileGateSceneDecoratorStrategy
import org.shareat.app.navscenedecorator.rememberResponsiveNavigationSceneDecoratorStrategy
import org.shareat.app.theme.AppTheme

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    AppTheme {
        SharedTransitionLayout {
            val navigationState = rememberNavigationState(
                startRoute = HomeKey,
                topLevelRoutes = setOf(HomeKey, ProfileKey),
            )
            val navigator = koinInject<Navigator> {
                parametersOf(navigationState)
            }

            val koin = getKoin()
            remember(navigator) { koin.declare(navigator) }

            val navigationSceneDecorator =
                rememberResponsiveNavigationSceneDecoratorStrategy<NavKey>(
                    navBar = { TopLevelNavigationBar(TOP_LEVEL_NAV_ITEMS, navigator) },
                    navRail = { TopLevelNavigationRail(TOP_LEVEL_NAV_ITEMS, navigator) },
                    sharedTransitionScope = this,
                )
            val restaurantProfileGateDecorator =
                rememberRestaurantProfileGateSceneDecoratorStrategy<NavKey>()
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
