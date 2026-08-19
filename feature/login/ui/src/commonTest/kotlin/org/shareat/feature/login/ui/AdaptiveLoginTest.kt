package org.shareat.feature.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.window.core.layout.WindowSizeClass
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AdaptiveLoginTest {

    @Test
    fun compactLayout_selectedForSmallWidth() = runComposeUiTest {
        setContent {
            LoginScreenContent(
                step = LoginStep.Welcome,
                uiState = LoginUiState(),
                windowSizeClass = WindowSizeClass(375f, 800f),
                onEmailFieldChange = {},
                onPasswordFieldChange = {},
                onDisplayNameFieldChange = {},
                onRoleChange = {},
                onLoginClick = {},
                onRequestPasswordRecovery = {},
                onGoTo = {}
            )
        }

        onNodeWithTag("login-compact-panel").assertExists()
        onNodeWithTag("login-two-pane-panel").assertDoesNotExist()
    }

    @Test
    fun twoPaneLayout_selectedForMediumWidth() = runComposeUiTest {
        setContent {
            LoginScreenContent(
                step = LoginStep.Welcome,
                uiState = LoginUiState(),
                windowSizeClass = WindowSizeClass(900f, 800f),
                onEmailFieldChange = {},
                onPasswordFieldChange = {},
                onDisplayNameFieldChange = {},
                onRoleChange = {},
                onLoginClick = {},
                onRequestPasswordRecovery = {},
                onGoTo = {}
            )
        }

        onNodeWithTag("login-two-pane-panel").assertExists()
        onNodeWithTag("login-compact-panel").assertDoesNotExist()
    }
}
