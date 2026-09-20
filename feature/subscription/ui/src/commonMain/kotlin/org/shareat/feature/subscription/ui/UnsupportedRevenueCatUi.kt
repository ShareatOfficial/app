package org.shareat.feature.subscription.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import shareat.feature.subscription.ui.generated.resources.Res
import shareat.feature.subscription.ui.generated.resources.subscription_close
import shareat.feature.subscription.ui.generated.resources.subscription_unsupported_platform

@Composable
internal fun UnsupportedRevenueCatUi(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.subscription_unsupported_platform))
        Button(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(Res.string.subscription_close))
        }
    }
}
