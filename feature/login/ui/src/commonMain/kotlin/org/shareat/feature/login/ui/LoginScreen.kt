package org.shareat.feature.login.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.koinInject
import org.shareat.app.domain.model.AccountRole

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

    LoginScreenContent(
        step = state.step,
        uiState = state,
        onEmailFieldChange = viewModel::onEmailFieldChange,
        onPasswordFieldChange = viewModel::onPasswordFieldChange,
        onDisplayNameFieldChange = viewModel::onDisplayNameFieldChange,
        onRoleChange = viewModel::onSelectRole,
        onLoginClick = viewModel::onLoginClick,
        onRequestPasswordRecovery = viewModel::onRequestPasswordRecovery,
        onGoTo = viewModel::goTo
    )
}

@Composable
internal fun LoginScreenContent(
    step: LoginStep,
    uiState: LoginUiState,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass,
    onEmailFieldChange: (String) -> Unit,
    onPasswordFieldChange: (String) -> Unit,
    onDisplayNameFieldChange: (String) -> Unit,
    onRoleChange: (AccountRole) -> Unit,
    onLoginClick: () -> Unit,
    onRequestPasswordRecovery: () -> Unit,
    onGoTo: (LoginStep) -> Unit
) {
    val currentFormContent = rememberUpdatedState<@Composable (LoginStep) -> Unit> { currentStep ->
        when (currentStep) {
            LoginStep.SignIn -> SignInScreen(
                email = uiState.email,
                password = uiState.password,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                recoverySent = uiState.recoverySent,
                onEmailFieldChange = onEmailFieldChange,
                onPasswordFieldChange = onPasswordFieldChange,
                onSignInClick = onLoginClick,
                onForgotPasswordClick = onRequestPasswordRecovery,
                onCreateAccountClick = { onGoTo(LoginStep.Register) },
                onBackClick = { onGoTo(LoginStep.Welcome) },
            )
            LoginStep.Register -> RegisterScreen(
                email = uiState.email,
                password = uiState.password,
                displayName = uiState.displayName,
                selectedRole = uiState.registrationRole,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onEmailFieldChange = onEmailFieldChange,
                onPasswordFieldChange = onPasswordFieldChange,
                onDisplayNameFieldChange = onDisplayNameFieldChange,
                onRoleChange = onRoleChange,
                onRegisterClick = onLoginClick,
                onSignInInsteadClick = { onGoTo(LoginStep.SignIn) },
                onBackClick = { onGoTo(LoginStep.Welcome) },
            )
            LoginStep.Welcome -> error("Welcome is owned by the host layout")
        }
    }

    val movableFormContent = remember {
        movableContentOf<LoginStep> { s ->
            currentFormContent.value(s)
        }
    }

    // TODO: guest browsing scope boundary no-op. This is an intentional no-op in both Compact and TwoPane layouts and is out of scope for this change.
    val onBrowseAsGuestClick = { /* TODO: guest browsing */ }

    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        TwoPaneLoginLayout(
            step = step,
            onGoTo = onGoTo,
            onBrowseAsGuestClick = onBrowseAsGuestClick,
            formContent = movableFormContent
        )
    } else {
        CompactLoginLayout(
            step = step,
            onGoTo = onGoTo,
            onBrowseAsGuestClick = onBrowseAsGuestClick,
            formContent = movableFormContent
        )
    }
}
