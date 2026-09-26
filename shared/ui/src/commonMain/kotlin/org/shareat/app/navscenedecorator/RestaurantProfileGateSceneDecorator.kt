package org.shareat.app.navscenedecorator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.app.auth.RestaurantProfileGateState
import org.jetbrains.compose.resources.stringResource
import shareat.shared.ui.generated.resources.Res
import shareat.shared.ui.generated.resources.restaurant_gate_error
import shareat.shared.ui.generated.resources.restaurant_gate_retry
import shareat.shared.ui.generated.resources.restaurant_gate_logout

/**
 * Replaces the fully decorated app scene while an authenticated restaurant account is being
 * checked. Apply this after navigation-chrome decorators so the correct landing is selected.
 */
@Composable
fun <T : Any> rememberRestaurantProfileGateSceneDecoratorStrategy(
    restaurantProfiles: RestaurantProfileCoordinator = koinInject(),
    gateStateOverride: RestaurantProfileGateState? = null,
): RestaurantProfileGateSceneDecoratorStrategy<T> {
    val observedGateState by restaurantProfiles.state.collectAsState()
    val gateState = gateStateOverride ?: observedGateState
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

            is RestaurantProfileGateState.Allowed -> scene.content()
            is RestaurantProfileGateState.Failure -> {
                RestaurantGateErrorScreen(onRetry, onLogout)
            }
        }
    }
}

@Composable
private fun RestaurantGateErrorScreen(onRetry: () -> Unit, onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.restaurant_gate_error))
            Button(onClick = onRetry) { Text(stringResource(Res.string.restaurant_gate_retry)) }
            TextButton(onClick = onLogout) { Text(stringResource(Res.string.restaurant_gate_logout)) }
        }
    }
}
