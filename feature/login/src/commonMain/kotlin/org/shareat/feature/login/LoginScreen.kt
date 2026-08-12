package org.shareat.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

var isUserLogged = false

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navigator: LoginNavigation = koinInject(),
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                isUserLogged = true
                navigator.onLoginSuccess()
            },
        ) {
            Text(text = "Login")
        }
    }
}
