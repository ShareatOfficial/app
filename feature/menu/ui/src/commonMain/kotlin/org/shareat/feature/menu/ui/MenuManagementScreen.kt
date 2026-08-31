package org.shareat.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.MenuPublicationState

@Composable
fun MenuManagementScreen(
    navigation: MenuManagementNavigation = koinInject(),
    viewModel: MenuManagementViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val picker = rememberFilePickerLauncher(type = FileKitType.Image, onResult = { file ->
        if (file != null) scope.launch { viewModel.onDishImageSelected(file.name, file.readBytes()) }
    })
    MenuManagementContent(state, viewModel::onAction, navigation::goBack, picker::launch)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MenuManagementContent(
    state: MenuManagementUiState,
    onAction: (MenuManagementAction) -> Unit,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Menu management") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } },
        )
    }) { padding ->
        when (state) {
            MenuManagementUiState.Loading -> Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { CircularProgressIndicator() }
            is MenuManagementUiState.Failure -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message); Spacer(Modifier.height(16.dp)); Button(onClick = { onAction(MenuManagementAction.Retry) }) { Text("Retry") }
            }
            is MenuManagementUiState.Editor -> Editor(state, onAction, Modifier.padding(padding), onPickImage)
        }
    }
}

@Composable
private fun Editor(state: MenuManagementUiState.Editor, onAction: (MenuManagementAction) -> Unit, modifier: Modifier, onPickImage: () -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(state.restaurantName, style = MaterialTheme.typography.titleMedium)
                Text(if (state.menuId == null) "Create your menu" else state.publicationState.label(), color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Menu details", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(state.name, { onAction(MenuManagementAction.NameChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Menu name") }, enabled = !state.isSaving)
                OutlinedTextField(state.description, { onAction(MenuManagementAction.DescriptionChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Description (optional)") }, enabled = !state.isSaving, minLines = 2)
                SaveButtons(state, onAction)
            } }
        }
        item { Text("Menu items", style = MaterialTheme.typography.titleLarge) }
        items(state.items, key = { it.dish.id.value }) { item ->
            Card { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(item.dish.name, style = MaterialTheme.typography.titleSmall); item.dish.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) } }
                    IconButton(onClick = { onAction(MenuManagementAction.EditDish(item.dish.id)) }) { Icon(Icons.Outlined.Edit, "Edit dish") }
                    IconButton(onClick = { onAction(MenuManagementAction.RemoveDish(item.dish.id)) }) { Icon(Icons.Outlined.RemoveCircleOutline, "Remove from menu") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(item.price, { onAction(MenuManagementAction.PriceChanged(item.dish.id, it)) }, Modifier.weight(1f), label = { Text("Price (€)") }, singleLine = true)
                    Spacer(Modifier.width(12.dp)); Text("Available"); Switch(item.isEnabled, { onAction(MenuManagementAction.ItemEnabledChanged(item.dish.id, it)) })
                }
                Row {
                    IconButton(onClick = { onAction(MenuManagementAction.MoveDish(item.dish.id, -1)) }) { Icon(Icons.Outlined.ArrowUpward, "Move up") }
                    IconButton(onClick = { onAction(MenuManagementAction.MoveDish(item.dish.id, 1)) }) { Icon(Icons.Outlined.ArrowDownward, "Move down") }
                    Spacer(Modifier.weight(1f)); OutlinedButton(onClick = { onAction(MenuManagementAction.RequestArchiveDish(item.dish.id)) }) { Text("Archive") }
                }
            } }
        }
        item { DishCatalog(state, onAction) }
        item { state.message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) } }
        item { Spacer(Modifier.height(24.dp)) }
    }
    state.dishEditor?.let { DishEditor(it, onAction, onPickImage) }
    state.confirm?.let { ConfirmationDialog(it, onAction) }
}

@Composable
private fun SaveButtons(state: MenuManagementUiState.Editor, onAction: (MenuManagementAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { onAction(MenuManagementAction.SaveDraft) }, enabled = !state.isSaving, modifier = Modifier.weight(1f)) { Text(if (state.isSaving) "Saving…" else "Save draft") }
        if (state.publicationState == MenuPublicationState.Published) OutlinedButton(onClick = { onAction(MenuManagementAction.Unpublish) }, enabled = !state.isSaving, modifier = Modifier.weight(1f)) { Text("Unpublish") }
        else OutlinedButton(onClick = { onAction(MenuManagementAction.Publish) }, enabled = !state.isSaving, modifier = Modifier.weight(1f)) { Text("Publish") }
    }
    if (state.menuId != null) OutlinedButton(onClick = { onAction(MenuManagementAction.RequestDeleteMenu) }, enabled = !state.isSaving) { Icon(Icons.Outlined.Delete, null); Spacer(Modifier.width(6.dp)); Text("Delete menu") }
}

@Composable
private fun DishCatalog(state: MenuManagementUiState.Editor, onAction: (MenuManagementAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Dish catalog", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)); Button(onClick = { onAction(MenuManagementAction.CreateDish) }) { Icon(Icons.Outlined.Add, null); Text("New dish") } }
        state.dishes.filterNot { dish -> state.items.any { it.dish.id == dish.id } }.forEach { dish ->
            Card { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(dish.name); Text(if (dish.isEnabled) "Available" else "Archived", style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = { onAction(MenuManagementAction.EditDish(dish.id)) }) { Icon(Icons.Outlined.Edit, "Edit") }
                Button(onClick = { onAction(MenuManagementAction.AddDish(dish.id)) }, enabled = dish.isEnabled) { Text("Add") }
            } }
        }
    }
}

@Composable
private fun DishEditor(editor: DishEditorState, onAction: (MenuManagementAction) -> Unit, onPickImage: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(MenuManagementAction.DismissDishEditor) },
        title = { Text(if (editor.id == null) "New dish" else "Edit dish") },
        text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(editor.name, { onAction(MenuManagementAction.DishNameChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Dish name") }) }
            item { OutlinedTextField(editor.description, { onAction(MenuManagementAction.DishDescriptionChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Description") }, minLines = 2) }
            item { Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (editor.pendingImage != null) "New photo selected" else if (editor.image != null) "Photo selected" else "No photo", Modifier.weight(1f))
                OutlinedButton(onClick = onPickImage) { Text("Choose photo") }
            } }
            if (editor.pendingImage != null || editor.image != null) {
                item { OutlinedTextField(editor.imageAlternativeText, { onAction(MenuManagementAction.ImageAlternativeTextChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Photo description (optional)") }) }
                item { OutlinedButton(onClick = { onAction(MenuManagementAction.RemoveImage) }) { Text("Remove photo") } }
            }
            item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Available", Modifier.weight(1f)); Switch(editor.enabled, { onAction(MenuManagementAction.DishEnabledChanged(it)) }) } }
            item { Text("Allergens", style = MaterialTheme.typography.titleMedium) }
            items(EuAllergen.entries) { allergen -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(allergen in editor.allergens, { onAction(MenuManagementAction.AllergenChanged(allergen, it)) }); Text(allergen.label()) } }
            item { OutlinedTextField(editor.allergenNote, { onAction(MenuManagementAction.AllergenNoteChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Allergen note (optional)") }) }
            editor.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        } },
        confirmButton = { Button(onClick = { onAction(MenuManagementAction.SaveDish) }, enabled = !editor.isSaving) { Text(if (editor.isSaving) "Saving…" else "Save") } },
        dismissButton = { OutlinedButton(onClick = { onAction(MenuManagementAction.DismissDishEditor) }) { Text("Cancel") } },
    )
}

@Composable
private fun ConfirmationDialog(confirm: Confirmation, onAction: (MenuManagementAction) -> Unit) = AlertDialog(
    onDismissRequest = { onAction(MenuManagementAction.DismissConfirmation) },
    title = { Text("Confirm action") },
    text = { Text(when (confirm) { is Confirmation.Archive -> "Archive this dish and remove it from the menu? Reviews will be kept."; is Confirmation.DeleteDish -> "Permanently delete this dish? Dishes with reviews cannot be deleted."; Confirmation.DeleteMenu -> "Delete this menu? Catalog dishes will be kept." }) },
    confirmButton = { Button(onClick = { onAction(MenuManagementAction.Confirm) }) { Text("Confirm") } },
    dismissButton = { OutlinedButton(onClick = { onAction(MenuManagementAction.DismissConfirmation) }) { Text("Cancel") } },
)

private fun MenuPublicationState.label() = when (this) {
    MenuPublicationState.Draft -> "Draft"
    MenuPublicationState.Published -> "Published"
    MenuPublicationState.Unpublished -> "Unpublished"
    MenuPublicationState.Disabled -> "Disabled"
}

private fun EuAllergen.label(): String = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
