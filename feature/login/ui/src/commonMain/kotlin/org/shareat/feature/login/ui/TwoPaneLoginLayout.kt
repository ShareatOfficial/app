package org.shareat.feature.login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import shareat.feature.login.ui.generated.resources.Res
import shareat.feature.login.ui.generated.resources.high_quality_immersive_vertical_food_photography_for_a_restaurant_app_welcome

private val welcomeBackground: DrawableResource
    get() = Res.drawable.high_quality_immersive_vertical_food_photography_for_a_restaurant_app_welcome

@Composable
internal fun TwoPaneLoginLayout(
    step: LoginStep,
    onGoTo: (LoginStep) -> Unit,
    onBrowseAsGuestClick: () -> Unit,
    formContent: @Composable (LoginStep) -> Unit
) {
    Row(Modifier.fillMaxSize().testTag("login-two-pane-panel")) {
        // Photo pane
        Box(Modifier.weight(0.45f).fillMaxHeight()) {
            Image(
                painter = painterResource(welcomeBackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            if (step == LoginStep.Welcome) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LoginWelcomeHero(
                        headlineColor = Color.White,
                        subtitleColor = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Content pane
        Box(
            modifier = Modifier.weight(0.55f),
            contentAlignment = Alignment.Center
        ) {
            Surface {
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                        }
                    ) { currentStep ->
                        if (currentStep == LoginStep.Welcome) {
                            LoginWelcomeActions(
                                onSignInClick = { onGoTo(LoginStep.SignIn) },
                                onRegisterClick = { onGoTo(LoginStep.Register) },
                                onBrowseAsGuestClick = onBrowseAsGuestClick
                            )
                        } else {
                            formContent(currentStep)
                        }
                    }
                }
            }
        }
    }
}
