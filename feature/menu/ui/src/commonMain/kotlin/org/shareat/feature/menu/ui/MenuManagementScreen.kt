package org.shareat.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import shareat.feature.menu.ui.generated.resources.Res
import shareat.feature.menu.ui.generated.resources.menu_back
import shareat.feature.menu.ui.generated.resources.menu_create
import shareat.feature.menu.ui.generated.resources.menu_creating
import shareat.feature.menu.ui.generated.resources.menu_description
import shareat.feature.menu.ui.generated.resources.menu_empty_body
import shareat.feature.menu.ui.generated.resources.menu_empty_title
import shareat.feature.menu.ui.generated.resources.menu_existing_badge
import shareat.feature.menu.ui.generated.resources.menu_existing_body
import shareat.feature.menu.ui.generated.resources.menu_existing_title
import shareat.feature.menu.ui.generated.resources.menu_loading
import shareat.feature.menu.ui.generated.resources.menu_name
import shareat.feature.menu.ui.generated.resources.menu_retry
import shareat.feature.menu.ui.generated.resources.menu_title

@Composable
fun MenuManagementScreen(
    navigation: MenuManagementNavigation = koinInject(),
    viewModel: MenuManagementViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    MenuManagementContent(
        state = state,
        onBack = navigation::goBack,
        onNameChanged = viewModel::onNameChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onCreate = viewModel::create,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MenuManagementContent(
    state: MenuManagementUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCreate: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.menu_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.menu_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                state.isLoading -> Loading()
                state.existingMenu != null -> ExistingMenu(state.existingMenu.name, state.existingMenu.description)
                else -> CreateMenuForm(
                    state = state,
                    onNameChanged = onNameChanged,
                    onDescriptionChanged = onDescriptionChanged,
                    onCreate = onCreate,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun Loading() = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(stringResource(Res.string.menu_loading))
}

@Composable
private fun CreateMenuForm(
    state: MenuManagementUiState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCreate: () -> Unit,
    onRetry: () -> Unit,
) = Column(
    modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.height(44.dp))
    Text(stringResource(Res.string.menu_empty_title), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(Res.string.menu_empty_body), style = MaterialTheme.typography.bodyLarge)
    OutlinedTextField(
        value = state.name,
        onValueChange = onNameChanged,
        label = { Text(stringResource(Res.string.menu_name)) },
        isError = state.nameError != null,
        supportingText = state.nameError?.let { { Text(it) } },
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChanged,
        label = { Text(stringResource(Res.string.menu_description)) },
        minLines = 3,
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth(),
    )
    state.errorMessage?.let {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(it, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetry, enabled = !state.isSaving) {
                    Text(stringResource(Res.string.menu_retry))
                }
            }
        }
    }
    Button(
        onClick = onCreate,
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.menu_creating))
        } else Text(stringResource(Res.string.menu_create))
    }
}

@Composable
private fun ExistingMenu(name: String, description: String?) = Card(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.menu_existing_badge), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(name, style = MaterialTheme.typography.headlineSmall)
        description?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        Text(stringResource(Res.string.menu_existing_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(Res.string.menu_existing_body), style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun EmptyPreview() = MenuManagementContent(
    state = MenuManagementUiState(isLoading = false),
    onBack = {}, onNameChanged = {}, onDescriptionChanged = {}, onCreate = {}, onRetry = {},
)
