package org.shareat.feature.restauranthome.ui.restauranthome.composables.sheetContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergens
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_add_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_change_image
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description_placeholder
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name_placeholder
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_label
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_placeholder
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_save
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_saving

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditDishContent(
    dish: RestaurantDish?,
    form: DishEditFormUiState,
    onImageChange: () -> Unit,
    onDishNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val changeImageLabel = stringResource(Res.string.restaurant_home_change_image)
    val isEnabled = !form.isSaving
    val isCreating = dish == null

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(
                if (isCreating) {
                    Res.string.restaurant_home_add_dish
                } else {
                    Res.string.restaurant_home_edit_dish
                },
            ),
            style = MaterialTheme.typography.headlineSmall,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            RestaurantImage(
                imageUrl = form.imageUrl,
                contentDescription = form.name,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            FilledTonalIconButton(
                onClick = {
                    focusManager.clearFocus()
                    onImageChange()
                },
                enabled = isEnabled,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = changeImageLabel,
                )
            }
        }

        OutlinedTextField(
            value = form.name,
            onValueChange = onDishNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_dish_name)) },
            placeholder = if (isCreating) {
                { Text(stringResource(Res.string.restaurant_home_dish_name_placeholder)) }
            } else {
                null
            },
            isError = form.validation.nameInvalid,
            supportingText = if (form.validation.nameInvalid) {
                { Text(stringResource(Res.string.restaurant_home_dish_name_invalid)) }
            } else {
                null
            },
            enabled = isEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
            ),
        )

        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_description)) },
            placeholder = if (isCreating) {
                { Text(stringResource(Res.string.restaurant_home_description_placeholder)) }
            } else {
                null
            },
            enabled = isEnabled,
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
            ),
        )

        OutlinedTextField(
            value = form.price,
            onValueChange = onPriceChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_price_label)) },
            placeholder = if (isCreating) {
                { Text(stringResource(Res.string.restaurant_home_price_placeholder)) }
            } else {
                null
            },
            suffix = { Text("€") },
            isError = form.validation.priceInvalid,
            supportingText = if (form.validation.priceInvalid) {
                { Text(stringResource(Res.string.restaurant_home_price_invalid)) }
            } else {
                null
            },
            enabled = isEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(Res.string.restaurant_home_allergens))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EuAllergen.entries.forEach { allergen ->
                    FilterChip(
                        selected = allergen in form.allergens,
                        onClick = {
                            focusManager.clearFocus()
                            onAllergenClick(allergen)
                        },
                        enabled = isEnabled,
                        label = { Text(allergen.label()) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }

        form.error?.let { error ->
            Text(
                text = error.label(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus(force = true)
                onSaveClick()
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(
                    if (form.isSaving) {
                        Res.string.restaurant_home_saving
                    } else {
                        Res.string.restaurant_home_save
                    },
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun EditDishContentPreview() {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant
        val dish = restaurant.dishes.first()
        EditDishContentPreviewHost(dish = dish)
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun AddDishContentPreview() {
    ShareatTheme {
        EditDishContentPreviewHost(dish = null)
    }
}

@Composable
private fun EditDishContentPreviewHost(dish: RestaurantDish?) {
    var form by remember(dish) {
        mutableStateOf(
            DishEditFormUiState(
                dishId = dish?.id,
                name = dish?.name.orEmpty(),
                description = dish?.description.orEmpty(),
                price = dish?.priceMinorUnits?.toEditablePrice().orEmpty(),
                allergens = dish?.allergens.orEmpty(),
                imageUrl = dish?.imageUrl,
                isPublished = dish?.isPublished ?: false,
            ),
        )
    }

    EditDishContent(
        dish = dish,
        form = form,
        onImageChange = {},
        onDishNameChange = { form = form.copy(name = it) },
        onDescriptionChange = { form = form.copy(description = it) },
        onPriceChange = { form = form.copy(price = it) },
        onSaveClick = {},
        onAllergenClick = { allergen ->
            form = form.copy(
                allergens = if (allergen in form.allergens) {
                    form.allergens - allergen
                } else {
                    form.allergens + allergen
                },
            )
        },
        modifier = Modifier.padding(16.dp),
    )
}
