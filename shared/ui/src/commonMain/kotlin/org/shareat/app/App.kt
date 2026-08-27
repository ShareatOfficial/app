package org.shareat.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.shareat.app.navigation.LocalNavigator
import org.shareat.app.auth.SessionCoordinator
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.navigation.HomeKey
import org.shareat.feature.profile.navigation.ProfileKey
import org.shareat.app.navigation.rememberNavigationState
import org.shareat.app.navigation.toEntries
import org.shareat.app.navscenedecorator.TOP_LEVEL_NAV_ITEMS
import org.shareat.app.navscenedecorator.TopLevelNavigationBar
import org.shareat.app.navscenedecorator.TopLevelNavigationRail
import org.shareat.app.navscenedecorator.rememberResponsiveNavigationSceneDecoratorStrategy
import org.shareat.app.theme.AppTypography

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    MaterialTheme(
        typography = AppTypography(),
    ) {
        val sessionCoordinator = koinInject<SessionCoordinator>()
        val sessionState by sessionCoordinator.state.collectAsState()
        if (sessionState is org.shareat.app.domain.model.AuthSessionState.Initializing) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@MaterialTheme
        }
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
            val entryProvider = koinEntryProvider<NavKey>()

            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    sceneDecoratorStrategies = listOf(navigationSceneDecorator),
                    sharedTransitionScope = this,
                    onBack = navigator::goBack,
                )
            }
        }
    }
}
