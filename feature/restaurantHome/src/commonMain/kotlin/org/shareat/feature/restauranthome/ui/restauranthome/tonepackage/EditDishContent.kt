package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergens
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_change_image
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_dish_name_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_price_label

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditDishContent(
    form: DishEditFormUiState,
    onImageChange: () -> Unit,
    onDishNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val changeImageLabel = stringResource(Res.string.restaurant_home_change_image)
    val isEnabled = !form.isSaving

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun EditDishContentPreview() {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant
        val dish = restaurant.dishes.first()
        var form by remember {
            mutableStateOf(
                DishEditFormUiState(
                    dishId = dish.id,
                    name = dish.name,
                    description = dish.description.orEmpty(),
                    price = dish.priceMinorUnits.toEditablePrice(),
                    allergens = dish.allergens,
                    imageUrl = dish.imageUrl,
                    isPublished = dish.isPublished,
                ),
            )
        }

        EditDishContent(
            form = form,
            onImageChange = {},
            onDishNameChange = { form = form.copy(name = it) },
            onDescriptionChange = { form = form.copy(description = it) },
            onPriceChange = { form = form.copy(price = it) },
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
}
