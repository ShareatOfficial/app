package org.shareat.feature.profile.ui.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val TERMS_AND_CONDITIONS_URL =
    "https://shareatofficial.github.io/app/terms-and-conditions.html"

@Composable
fun TermsAndConditionsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = onBackClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Go back",
                )
            }
            Text(
                text = "Terms and Conditions",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
            TermsAndConditionsContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
expect fun TermsAndConditionsContent(modifier: Modifier = Modifier)
