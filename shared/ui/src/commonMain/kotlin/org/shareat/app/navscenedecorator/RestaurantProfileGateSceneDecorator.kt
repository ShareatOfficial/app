package org.shareat.app.navscenedecorator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.app.auth.RestaurantProfileGateState
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingGateErrorScreen
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingScreen

/**
 * Replaces the fully decorated app scene while an existing restaurant account must create its
 * profile. Apply this after navigation-chrome decorators so onboarding never exposes app chrome.
 */
@Composable
fun <T : Any> rememberRestaurantProfileGateSceneDecoratorStrategy(
    restaurantProfiles: RestaurantProfileCoordinator = koinInject(),
): RestaurantProfileGateSceneDecoratorStrategy<T> {
    val gateState by restaurantProfiles.state.collectAsState()
    val currentGateState = rememberUpdatedState(gateState)
    val scope = rememberCoroutineScope()
    val onLogout: () -> Unit = remember(restaurantProfiles, scope) {
        { launchSignOut(scope, restaurantProfiles) }
    }

    return remember(restaurantProfiles, onLogout) {
        RestaurantProfileGateSceneDecoratorStrategy(
            gateState = { currentGateState.value },
            onRetry = restaurantProfiles::retry,
            onLogout = onLogout,
        )
    }
}

private fun launchSignOut(
    scope: CoroutineScope,
    restaurantProfiles: RestaurantProfileCoordinator,
) {
    scope.launch { restaurantProfiles.signOut() }
}

class RestaurantProfileGateSceneDecoratorStrategy<T : Any>(
    private val gateState: () -> RestaurantProfileGateState,
    private val onRetry: () -> Unit,
    private val onLogout: () -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        RestaurantProfileGateScene(scene, gateState, onRetry, onLogout)
}

private class RestaurantProfileGateScene<T : Any>(
    private val scene: Scene<T>,
    private val gateState: () -> RestaurantProfileGateState,
    private val onRetry: () -> Unit,
    private val onLogout: () -> Unit,
) : Scene<T> by scene {
    override val key = RestaurantProfileGateScene::class to scene.key

    override val content: @Composable () -> Unit = {
        when (gateState()) {
            RestaurantProfileGateState.Checking -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            RestaurantProfileGateState.Allowed -> scene.content()
            RestaurantProfileGateState.OnboardingRequired -> RestaurantOnboardingScreen()
            is RestaurantProfileGateState.Failure -> {
                RestaurantOnboardingGateErrorScreen(onRetry, onLogout)
            }
        }
    }
}
