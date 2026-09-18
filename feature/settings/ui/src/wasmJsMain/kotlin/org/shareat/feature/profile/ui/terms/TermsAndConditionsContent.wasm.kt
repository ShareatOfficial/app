package org.shareat.feature.profile.ui.terms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
actual fun TermsAndConditionsContent(modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Abre los términos en tu navegador:", style = MaterialTheme.typography.bodyLarge)
        Text(TERMS_AND_CONDITIONS_URL, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { uriHandler.openUri(TERMS_AND_CONDITIONS_URL) }) {
            Text("Abrir términos")
        }
    }
}
