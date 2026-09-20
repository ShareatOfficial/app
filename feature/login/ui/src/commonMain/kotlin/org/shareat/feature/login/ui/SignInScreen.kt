package org.shareat.feature.login.ui

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
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.login.ui.components.AuthTextField
import org.shareat.feature.login.ui.components.label
import org.shareat.feature.login.ui.model.LoginError
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.login.ui.generated.resources.Res
import shareat.feature.login.ui.generated.resources.login_back
import shareat.feature.login.ui.generated.resources.login_create_account_instead
import shareat.feature.login.ui.generated.resources.login_email
import shareat.feature.login.ui.generated.resources.login_forgot_password
import shareat.feature.login.ui.generated.resources.login_password
import shareat.feature.login.ui.generated.resources.login_recovery_sent
import shareat.feature.login.ui.generated.resources.login_sign_in
import shareat.feature.login.ui.generated.resources.login_sign_in_title

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SignInScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    error: LoginError?,
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
            Text(text = stringResource(Res.string.login_back))
        }
        Text(
            text = stringResource(Res.string.login_sign_in_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        AuthTextField(
            value = email,
            onValueChange = onEmailFieldChange,
            label = stringResource(Res.string.login_email),
            enabled = !isLoading,
            keyboardType = KeyboardType.Email,
        )
        AuthTextField(
            value = password,
            onValueChange = onPasswordFieldChange,
            label = stringResource(Res.string.login_password),
            enabled = !isLoading,
            isPassword = true,
            imeAction = ImeAction.Done,
        )
        error?.let {
            Text(
                text = it.label(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (recoverySent) {
            Text(
                text = stringResource(Res.string.login_recovery_sent),
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
                Text(text = stringResource(Res.string.login_sign_in))
            }
        }
        TextButton(
            onClick = onForgotPasswordClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(Res.string.login_forgot_password))
        }
        TextButton(
            onClick = onCreateAccountClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(Res.string.login_create_account_instead))
        }
        // TODO: provider sign-in (Google, Apple, or both depending on the platform).
    }
}

@Preview
@Composable
private fun SignInScreenPreview() {
    ShareatTheme {
        SignInScreen(
            email = "ada@shareat.org",
            password = "hunter2000",
            isLoading = false,
            error = null,
            recoverySent = false,
            onEmailFieldChange = {},
            onPasswordFieldChange = {},
            onSignInClick = {},
            onForgotPasswordClick = {},
            onCreateAccountClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun SignInScreenLoadingPreview() {
    ShareatTheme {
        SignInScreen(
            email = "ada@shareat.org",
            password = "hunter2000",
            isLoading = true,
            error = null,
            recoverySent = false,
            onEmailFieldChange = {},
            onPasswordFieldChange = {},
            onSignInClick = {},
            onForgotPasswordClick = {},
            onCreateAccountClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun SignInScreenErrorPreview() {
    ShareatTheme {
        SignInScreen(
            email = "ada@shareat.org",
            password = "nope",
            isLoading = false,
            error = LoginError.INVALID_CREDENTIALS,
            recoverySent = false,
            onEmailFieldChange = {},
            onPasswordFieldChange = {},
            onSignInClick = {},
            onForgotPasswordClick = {},
            onCreateAccountClick = {},
            onBackClick = {},
        )
    }
}
