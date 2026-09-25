package org.shareat.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.login.ui.generated.resources.Res
import shareat.feature.login.ui.generated.resources.login_create_account
import shareat.feature.login.ui.generated.resources.login_sign_in
import shareat.feature.login.ui.generated.resources.login_welcome_subtitle
import shareat.feature.login.ui.generated.resources.login_welcome_title

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginWelcome(
    onSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.login_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.login_welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonShapes(
                shape = RoundedCornerShape(8.dp),
                pressedShape = MaterialTheme.shapes.large,
            ),
        ) {
            Text(text = stringResource(Res.string.login_create_account))
        }
        OutlinedButton(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonShapes(
                shape = RoundedCornerShape(8.dp), // ideally this should be in the Material theme by default
                pressedShape = MaterialTheme.shapes.large,
            )
        ) {
            Text(text = stringResource(Res.string.login_sign_in))
        }
    }
}

@Preview
@Composable
private fun LoginWelcomePreview() {
    ShareatTheme {
        LoginWelcome(
            onSignInClick = {},
            onRegisterClick = {},
        )
    }
}
