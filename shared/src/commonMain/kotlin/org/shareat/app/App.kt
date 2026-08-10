package org.shareat.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.shareat.app.navscenedecorator.Navigator
import org.shareat.app.navscenedecorator.TOP_LEVEL_NAV_ITEMS
import org.shareat.app.navscenedecorator.TopLevelNavigationBar
import org.shareat.app.navscenedecorator.TopLevelNavigationRail
import org.shareat.app.navscenedecorator.rememberNavigationState
import org.shareat.app.navscenedecorator.rememberResponsiveNavigationSceneDecoratorStrategy
import org.shareat.app.navscenedecorator.toEntries
import org.shareat.feature.home.Home
import org.shareat.feature.home.HomeRoute
import org.shareat.feature.profile.Profile
import org.shareat.feature.profile.ProfileRoute


// TODO Add koin as dependency injection framework and create a
//  navigation interface for each sceen and add the implementation with the key in the
//  in the app module while the entry provider is in the feature module
@Composable
@Preview
fun App() {
    MaterialTheme {
        SharedTransitionLayout {
            val navigationState = rememberNavigationState(
                startRoute = HomeRoute,
                topLevelRoutes = setOf(HomeRoute, ProfileRoute),
            )
            val navigator = remember(navigationState) { Navigator(navigationState) }
            val navigationSceneDecorator =
                rememberResponsiveNavigationSceneDecoratorStrategy<NavKey>(
                    navBar = { TopLevelNavigationBar(TOP_LEVEL_NAV_ITEMS, navigator) },
                    navRail = { TopLevelNavigationRail(TOP_LEVEL_NAV_ITEMS, navigator) },
                    sharedTransitionScope = this,
                )
            val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
                entry<HomeRoute> { Home() }
                entry<ProfileRoute> { Profile() }
            }

            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                sceneDecoratorStrategies = listOf(navigationSceneDecorator),
                sharedTransitionScope = this,
                onBack = navigator::goBack,
            )
        }
    }
}
