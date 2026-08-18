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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

private const val ContentFadeOutMillis = 140

private const val PanelResizeMillis = 320

private const val ContentSlideInMillis = 260

@Composable
fun LoginScreen(
    navigator: LoginNavigation = koinInject(),
    viewModel: LoginViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) {
            navigator.onLoginSuccess()
        }
    }

    LoginBackground(modifier = Modifier) {
        AnimatedContent(
            targetState = state.step,
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
                    onSignInClick = { viewModel.goTo(LoginStep.SignIn) },
                    onRegisterClick = { viewModel.goTo(LoginStep.Register) },
                    onBrowseAsGuestClick = { /* TODO: guest browsing */ },
                )

                LoginStep.SignIn -> SignInScreen(
                    email = state.email,
                    password = state.password,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    recoverySent = state.recoverySent,
                    onEmailFieldChange = viewModel::onEmailFieldChange,
                    onPasswordFieldChange = viewModel::onPasswordFieldChange,
                    onSignInClick = viewModel::onLoginClick,
                    onForgotPasswordClick = viewModel::onRequestPasswordRecovery,
                    onCreateAccountClick = { viewModel.goTo(LoginStep.Register) },
                    onBackClick = { viewModel.goTo(LoginStep.Welcome) },
                )

                LoginStep.Register -> RegisterScreen(
                    email = state.email,
                    password = state.password,
                    displayName = state.displayName,
                    selectedRole = state.registrationRole,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    onEmailFieldChange = viewModel::onEmailFieldChange,
                    onPasswordFieldChange = viewModel::onPasswordFieldChange,
                    onDisplayNameFieldChange = viewModel::onDisplayNameFieldChange,
                    onRoleChange = viewModel::onSelectRole,
                    onRegisterClick = viewModel::onLoginClick,
                    onSignInInsteadClick = { viewModel.goTo(LoginStep.SignIn) },
                    onBackClick = { viewModel.goTo(LoginStep.Welcome) },
                )
            }
        }
    }
}
