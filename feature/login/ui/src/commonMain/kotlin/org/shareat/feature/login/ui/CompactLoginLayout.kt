package org.shareat.feature.login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

private const val ContentFadeOutMillis = 140

private const val PanelResizeMillis = 320

private const val ContentSlideInMillis = 260

@Composable
internal fun CompactLoginLayout(
    step: LoginStep,
    onGoTo: (LoginStep) -> Unit,
    onBrowseAsGuestClick: () -> Unit,
    formContent: @Composable (LoginStep) -> Unit
) {
    LoginBackground(modifier = Modifier.testTag("login-compact-panel")) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val towardsDetail = targetState != LoginStep.Welcome
                val offset = if (towardsDetail) 1 else -1
                val enter = slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = ContentSlideInMillis,
                        delayMillis = PanelResizeMillis,
                        easing = LinearOutSlowInEasing,
                    ),
                ) { width -> offset * width / 4 } + fadeIn(
                    animationSpec = tween(
                        durationMillis = ContentSlideInMillis,
                        delayMillis = PanelResizeMillis,
                        easing = LinearEasing,
                    ),
                )
                val exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = ContentFadeOutMillis,
                        easing = LinearEasing,
                    ),
                )
                enter togetherWith exit using SizeTransform(clip = false) { _, _ ->
                    tween(durationMillis = PanelResizeMillis, easing = FastOutSlowInEasing)
                }
            },
        ) { currentStep ->
            when (currentStep) {
                LoginStep.Welcome -> LoginWelcome(
                    onSignInClick = { onGoTo(LoginStep.SignIn) },
                    onRegisterClick = { onGoTo(LoginStep.Register) },
                    onBrowseAsGuestClick = onBrowseAsGuestClick,
                )
                LoginStep.SignIn -> formContent(currentStep)
                LoginStep.Register -> formContent(currentStep)
            }
        }
    }
}
