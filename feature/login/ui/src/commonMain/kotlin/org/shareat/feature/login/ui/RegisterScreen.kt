package org.shareat.feature.login.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.app.domain.model.AccountRole
import org.shareat.feature.login.ui.components.AuthTextField
import org.shareat.shared.designsystem.theme.ShareatTheme

private const val MIN_PASSWORD_LENGTH = 8

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RegisterScreen(
    email: String,
    password: String,
    displayName: String,
    selectedRole: AccountRole,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailFieldChange: (String) -> Unit,
    onPasswordFieldChange: (String) -> Unit,
    onDisplayNameFieldChange: (String) -> Unit,
    onRoleChange: (AccountRole) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInInsteadClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The view model only tracks the password the account is created with; the confirmation is
    // UI-only and never leaves this screen.
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val passwordsMismatch = confirmPassword.isNotEmpty() && confirmPassword != password

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBackClick, enabled = !isLoading) {
            Text(text = "Back") // extract string resource
        }
        Text(
            text = "Create your account", // extract string resource
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
            supportingText = "At least $MIN_PASSWORD_LENGTH characters", // extract string resource
        )
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password", // extract string resource
            enabled = !isLoading,
            isPassword = true,
            isError = passwordsMismatch,
            supportingText = "Passwords don't match".takeIf { passwordsMismatch }, // extract string resource
        )
        RoleField(
            selectedRole = selectedRole,
            onRoleChange = onRoleChange,
            enabled = !isLoading,
        )
        AnimatedVisibility(
            visible = selectedRole == AccountRole.Customer,
            enter = expandVertically(),
            exit = shrinkVertically(),
            label = "DisplayNameAnimation"
        ) {
            AuthTextField(
                value = displayName,
                onValueChange = onDisplayNameFieldChange,
                label = "Display name", // extract string resource
                enabled = !isLoading,
                imeAction = ImeAction.Done,
            )
        }
        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !passwordsMismatch,
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
                Text(text = "Create account") // extract string resource
            }
        }
        TextButton(
            onClick = onSignInInsteadClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = "Already have an account? Sign in") // extract string resource
        }
    }
}

/** One account role per account, [AccountRole.Customer] preselected. */
@Composable
private fun RoleField(
    selectedRole: AccountRole,
    onRoleChange: (AccountRole) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "I'm signing up as", // extract string resource
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AccountRole.entries.forEachIndexed { index, role ->
                SegmentedButton(
                    selected = role == selectedRole,
                    onClick = { onRoleChange(role) },
                    shape = SegmentedButtonDefaults.itemShape(index, AccountRole.entries.size),
                    enabled = enabled,
                    label = { Text(role.label) },
                )
            }
        }
        Text(
            text = selectedRole.roleHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// extract string resources
private val AccountRole.label: String
    get() = when (this) {
        AccountRole.Customer -> "Customer"
        AccountRole.Restaurant -> "Restaurant"
    }

private val AccountRole.roleHint: String
    get() = when (this) {
        AccountRole.Customer -> "Discover restaurants and review the dishes you try."
        AccountRole.Restaurant -> "Publish your restaurant profile and manage one menu."
    }

@Preview
@Composable
private fun RegisterScreenCustomerPreview() {
    ShareatTheme {
        RegisterScreen(
            email = "ada@shareat.org",
            password = "hunter2000",
            displayName = "Ada",
            selectedRole = AccountRole.Customer,
            isLoading = false,
            errorMessage = null,
            onEmailFieldChange = {},
            onPasswordFieldChange = {},
            onDisplayNameFieldChange = {},
            onRoleChange = {},
            onRegisterClick = {},
            onSignInInsteadClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun RegisterScreenRestaurantPreview() {
    ShareatTheme {
        RegisterScreen(
            email = "hola@casanaranja.es",
            password = "hunter2000",
            displayName = "",
            selectedRole = AccountRole.Restaurant,
            isLoading = false,
            errorMessage = null,
            onEmailFieldChange = {},
            onPasswordFieldChange = {},
            onDisplayNameFieldChange = {},
            onRoleChange = {},
            onRegisterClick = {},
            onSignInInsteadClick = {},
            onBackClick = {},
        )
    }
}
