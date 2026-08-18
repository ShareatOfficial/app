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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginWelcome(
    onSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onBrowseAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Good food starts with a good choice.", // extract string resource
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Discover trusted restaurants and the dishes people come back for.", // extract string resource
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
            Text(text = "Create account") // extract string resource
        }
        OutlinedButton(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonShapes(
                shape = RoundedCornerShape(8.dp), // ideally this should be in the Material theme by default
                pressedShape = MaterialTheme.shapes.large,
            )
        ) {
            Text(text = "Sign in") // extract string resource
        }
        TextButton(
            onClick = onBrowseAsGuestClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = "Browse without an account") // extract string resource
        }
    }
}

@Preview
@Composable
private fun LoginWelcomePreview() {
    LoginWelcome(
        onSignInClick = {},
        onRegisterClick = {},
        onBrowseAsGuestClick = {},
    )
}
