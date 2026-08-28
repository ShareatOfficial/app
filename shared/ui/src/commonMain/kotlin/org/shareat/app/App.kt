package org.shareat.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.app.auth.RestaurantProfileGateState
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.navigation.HomeKey
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.app.navigation.rememberNavigationState
import org.shareat.app.navigation.toEntries
import org.shareat.app.navscenedecorator.TOP_LEVEL_NAV_ITEMS
import org.shareat.app.navscenedecorator.TopLevelNavigationBar
import org.shareat.app.navscenedecorator.TopLevelNavigationRail
import org.shareat.app.navscenedecorator.rememberResponsiveNavigationSceneDecoratorStrategy
import org.shareat.app.theme.AppTheme
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingGateErrorScreen
import kotlinx.coroutines.launch

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    AppTheme {
        val sessionCoordinator = koinInject<SessionCoordinator>()
        val restaurantProfiles = koinInject<RestaurantProfileCoordinator>()
        val sessionState by sessionCoordinator.state.collectAsState()
        val restaurantProfileState by restaurantProfiles.state.collectAsState()
        val scope = rememberCoroutineScope()
        if (
            sessionState is org.shareat.app.domain.model.AuthSessionState.Initializing ||
            restaurantProfileState is RestaurantProfileGateState.Checking
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@AppTheme
        }
        if (restaurantProfileState is RestaurantProfileGateState.Failure) {
            RestaurantOnboardingGateErrorScreen(
                onRetry = restaurantProfiles::retry,
                onLogout = { scope.launch { restaurantProfiles.signOut() } },
            )
            return@AppTheme
        }
        SharedTransitionLayout {
            val navigationState = rememberNavigationState(
                startRoute = HomeKey,
                topLevelRoutes = setOf(HomeKey, ProfileKey),
            )
            val navigator = koinInject<Navigator> {
                parametersOf(navigationState)
            }

            LaunchedEffect(restaurantProfileState, navigator) {
                if (restaurantProfileState is RestaurantProfileGateState.OnboardingRequired) {
                    navigator.requireRestaurantOnboarding()
                }
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
                if (
                    restaurantProfileState is RestaurantProfileGateState.OnboardingRequired &&
                    !navigator.isRestaurantOnboardingVisible()
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
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
}
