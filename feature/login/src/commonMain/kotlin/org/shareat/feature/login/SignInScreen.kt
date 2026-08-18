package org.shareat.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.login.components.AuthTextField

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SignInScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    recoverySent: Boolean,
    onEmailFieldChange: (String) -> Unit,
    onPasswordFieldChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBackClick, enabled = !isLoading) {
            Text(text = "Back") // extract string resource
        }
        Text(
            text = "Welcome back", // extract string resource
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        AuthTextField(
            value = email,
            onValueChange = onEmailFieldChange,
            label = "Email", // extract string resource
            enabled = !isLoading,
            keyboardType = KeyboardType.Email,
        )
        AuthTextField(
            value = password,
            onValueChange = onPasswordFieldChange,
            label = "Password", // extract string resource
            enabled = !isLoading,
            isPassword = true,
            imeAction = ImeAction.Done,
        )
        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (recoverySent) {
            Text(
                text = "Check your email for the password recovery link.", // extract string resource
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shapes = ButtonShapes(
                shape = MaterialTheme.shapes.large,
                pressedShape = CircleShape,
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(text = "Sign in") // extract string resource
            }
        }
        TextButton(
            onClick = onForgotPasswordClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = "Forgot password?") // extract string resource
        }
        TextButton(
            onClick = onCreateAccountClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = "New to Shareat? Create account") // extract string resource
        }
        // TODO: provider sign-in (Google, Apple, or both depending on the platform).
    }
}

@Preview
@Composable
private fun SignInScreenPreview() {
    SignInScreen(
        email = "ada@shareat.org",
        password = "hunter2000",
        isLoading = false,
        errorMessage = null,
        recoverySent = false,
        onEmailFieldChange = {},
        onPasswordFieldChange = {},
        onSignInClick = {},
        onForgotPasswordClick = {},
        onCreateAccountClick = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun SignInScreenLoadingPreview() {
    SignInScreen(
        email = "ada@shareat.org",
        password = "hunter2000",
        isLoading = true,
        errorMessage = null,
        recoverySent = false,
        onEmailFieldChange = {},
        onPasswordFieldChange = {},
        onSignInClick = {},
        onForgotPasswordClick = {},
        onCreateAccountClick = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun SignInScreenErrorPreview() {
    SignInScreen(
        email = "ada@shareat.org",
        password = "nope",
        isLoading = false,
        errorMessage = "The email or password is incorrect.",
        recoverySent = false,
        onEmailFieldChange = {},
        onPasswordFieldChange = {},
        onSignInClick = {},
        onForgotPasswordClick = {},
        onCreateAccountClick = {},
        onBackClick = {},
    )
}
