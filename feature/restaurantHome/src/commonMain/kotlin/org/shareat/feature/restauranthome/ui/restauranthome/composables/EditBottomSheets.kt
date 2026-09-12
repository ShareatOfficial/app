package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantEditFormUiState
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergens
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_add_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_cancel
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_change_image
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_image_picker_unavailable
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_image_selected
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit_restaurant
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_locality
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_locality_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_name
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_label
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_postal_code
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_postal_code_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_publish_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_restaurant_name_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_region
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_save
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_saving
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_street
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_street_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_types_coming_soon
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_types_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantEditBottomSheet(
    form: RestaurantEditFormUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onLocalityChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRequestImage: (() -> Unit)?,
    onSave: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val dismissSheet = rememberDismissSheet(focusManager, onDismiss)
    val saveAndClearFocus = rememberSaveAndClearFocus(focusManager, onSave)

    ModalBottomSheet(onDismissRequest = dismissSheet) {
        EditSheetContent(
            title = stringResource(Res.string.restaurant_home_edit_restaurant),
            actions = { EditActions(isSaving = form.isSaving, onDismiss = dismissSheet, onSave = saveAndClearFocus) },
        ) {
            ImageChangeButton(
                onClick = onRequestImage?.let { { focusManager.clearFocus(); it() } },
                hasPendingImage = form.pendingImageUpload != null,
            )
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.restaurant_home_name)) },
                isError = form.validation.nameInvalid,
                supportingText = if (form.validation.nameInvalid) {{ Text(stringResource(Res.string.restaurant_home_restaurant_name_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.streetLine,
                onValueChange = onStreetChange,
                label = { Text(stringResource(Res.string.restaurant_home_street)) },
                isError = form.validation.streetInvalid,
                supportingText = if (form.validation.streetInvalid) {{ Text(stringResource(Res.string.restaurant_home_street_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.locality,
                onValueChange = onLocalityChange,
                label = { Text(stringResource(Res.string.restaurant_home_locality)) },
                isError = form.validation.localityInvalid,
                supportingText = if (form.validation.localityInvalid) {{ Text(stringResource(Res.string.restaurant_home_locality_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.postalCode,
                onValueChange = onPostalCodeChange,
                label = { Text(stringResource(Res.string.restaurant_home_postal_code)) },
                isError = form.validation.postalCodeInvalid,
                supportingText = if (form.validation.postalCodeInvalid) {{ Text(stringResource(Res.string.restaurant_home_postal_code_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.region,
                onValueChange = onRegionChange,
                label = { Text(stringResource(Res.string.restaurant_home_region)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(Res.string.restaurant_home_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = doneKeyboardAction(focusManager),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(Res.string.restaurant_home_types_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(Res.string.restaurant_home_types_coming_soon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            form.error?.let { Text(it.label(), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DishEditBottomSheet(
    form: DishEditFormUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    onPublishedChange: (Boolean) -> Unit,
    onRequestImage: (() -> Unit)?,
    onSave: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val dismissSheet = rememberDismissSheet(focusManager, onDismiss)
    val saveAndClearFocus = rememberSaveAndClearFocus(focusManager, onSave)

    ModalBottomSheet(onDismissRequest = dismissSheet) {
        EditSheetContent(
            title = stringResource(if (form.dishId == null) Res.string.restaurant_home_add_dish else Res.string.restaurant_home_edit_dish),
            actions = { EditActions(isSaving = form.isSaving, onDismiss = dismissSheet, onSave = saveAndClearFocus) },
        ) {
            ImageChangeButton(
                onClick = onRequestImage?.let { { focusManager.clearFocus(); it() } },
                hasPendingImage = form.pendingImageUpload != null,
            )
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.restaurant_home_dish_name)) },
                isError = form.validation.nameInvalid,
                supportingText = if (form.validation.nameInvalid) {{ Text(stringResource(Res.string.restaurant_home_restaurant_name_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(Res.string.restaurant_home_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = nextKeyboardAction(focusManager),
            )
            OutlinedTextField(
                value = form.price,
                onValueChange = onPriceChange,
                label = { Text(stringResource(Res.string.restaurant_home_price_label)) },
                isError = form.validation.priceInvalid,
                supportingText = if (form.validation.priceInvalid) {{ Text(stringResource(Res.string.restaurant_home_price_invalid)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = doneKeyboardAction(focusManager),
            )
            Text(stringResource(Res.string.restaurant_home_allergens), style = MaterialTheme.typography.titleSmall)
            AllergenPicker(
                selected = form.allergens,
                enabled = !form.isSaving,
                onAllergenClick = {
                    focusManager.clearFocus()
                    onAllergenClick(it)
                },
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.restaurant_home_publish_dish), style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = form.isPublished,
                    onCheckedChange = {
                        focusManager.clearFocus()
                        onPublishedChange(it)
                    },
                    enabled = !form.isSaving,
                )
            }
            form.error?.let { Text(it.label(), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun EditSheetContent(
    title: String,
    actions: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        // ModalBottomSheet already consumes the platform safe-area insets. Keeping only IME
        // padding here avoids a second navigation-bar inset while still lifting the actions.
        modifier = Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            content()
        }
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 12.dp),
        ) {
            actions()
        }
    }
}

@Composable
private fun ImageChangeButton(onClick: (() -> Unit)?, hasPendingImage: Boolean) {
    TextButton(onClick = { onClick?.invoke() }, enabled = onClick != null, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
        Text(stringResource(Res.string.restaurant_home_change_image))
        if (hasPendingImage) Text(" · ${stringResource(Res.string.restaurant_home_image_selected)}")
    }
    if (onClick == null) {
        Text(
            stringResource(Res.string.restaurant_home_image_picker_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AllergenPicker(selected: Set<EuAllergen>, enabled: Boolean, onAllergenClick: (EuAllergen) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EuAllergen.entries.forEach { allergen ->
            FilterChip(
                selected = allergen in selected,
                onClick = { onAllergenClick(allergen) },
                enabled = enabled,
                label = { Text(allergen.label()) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            )
        }
    }
}

private fun nameKeyboardOptions(imeAction: ImeAction): KeyboardOptions = KeyboardOptions(
    capitalization = KeyboardCapitalization.Words,
    keyboardType = KeyboardType.Text,
    imeAction = imeAction,
)

private fun nextKeyboardAction(focusManager: FocusManager) = androidx.compose.foundation.text.KeyboardActions(
    onNext = { focusManager.moveFocus(FocusDirection.Next) },
)

private fun doneKeyboardAction(focusManager: FocusManager) = androidx.compose.foundation.text.KeyboardActions(
    onDone = { focusManager.clearFocus() },
)

@Composable
private fun rememberDismissSheet(focusManager: FocusManager, onDismiss: () -> Unit): () -> Unit =
    androidx.compose.runtime.remember(focusManager, onDismiss) {
        {
            focusManager.clearFocus(force = true)
            onDismiss()
        }
    }

@Composable
private fun rememberSaveAndClearFocus(focusManager: FocusManager, onSave: () -> Unit): () -> Unit =
    androidx.compose.runtime.remember(focusManager, onSave) {
        {
            focusManager.clearFocus(force = true)
            onSave()
        }
    }

@Composable
private fun EditActions(isSaving: Boolean, onDismiss: () -> Unit, onSave: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
            Text(stringResource(Res.string.restaurant_home_cancel))
        }
        Button(onClick = onSave, enabled = !isSaving, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
            Text(stringResource(if (isSaving) Res.string.restaurant_home_saving else Res.string.restaurant_home_save))
        }
    }
}
