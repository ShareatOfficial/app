package org.shareat.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.shareat.app.domain.model.AccountRole

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navigator: LoginNavigation = koinInject(),
    viewModel: LoginViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) {
            viewModel.consumeAuthentication()
            navigator.onLoginSuccess()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (state.isRegistration) "Create your Shareat account" else "Sign in to Shareat")
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            singleLine = true,
            enabled = !state.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Password") },
            singleLine = true,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
        )
        if (state.isRegistration) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = state.registrationRole == AccountRole.Customer,
                    onClick = { viewModel.selectRole(AccountRole.Customer) },
                    label = { Text("Customer") },
                    enabled = !state.isLoading,
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.registrationRole == AccountRole.Restaurant,
                    onClick = { viewModel.selectRole(AccountRole.Restaurant) },
                    label = { Text("Restaurant") },
                    enabled = !state.isLoading,
                )
            }
            if (state.registrationRole == AccountRole.Customer) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.displayName,
                    onValueChange = viewModel::updateDisplayName,
                    label = { Text("Display name") },
                    singleLine = true,
                    enabled = !state.isLoading,
                )
            }
        }
        state.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it)
        }
        if (state.recoverySent) {
            Spacer(Modifier.height(12.dp))
            Text("Check your email for the password recovery link.")
        }
        Spacer(Modifier.height(20.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::submit,
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text(if (state.isRegistration) "Create account" else "Sign in")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.setRegistration(!state.isRegistration) },
            enabled = !state.isLoading,
        ) {
            Text(if (state.isRegistration) "I already have an account" else "Create an account")
        }
        if (!state.isRegistration) {
            TextButton(onClick = viewModel::requestPasswordRecovery, enabled = !state.isLoading) {
                Text("Forgot password?")
            }
        }
    }
}
