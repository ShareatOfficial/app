package org.shareat.feature.restauranthome.ui.restauranthome.composables.sheetContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_change_image
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_name
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_restaurant_name_invalid
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_save
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_saving


@Composable
internal fun EditMainInfoContent(
    restaurantName: String,
    description: String,
    restaurantImageUrl: String,
    onImageChange: () -> Unit,
    onRestaurantNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean = false,
    nameInvalid: Boolean = false,
    error: RestaurantHomeError? = null,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val changeImageLabel = stringResource(Res.string.restaurant_home_change_image)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            RestaurantImage(
                imageUrl = restaurantImageUrl,
                contentDescription = restaurantName,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            FilledTonalIconButton(
                onClick = {
                    focusManager.clearFocus()
                    onImageChange()
                },
                enabled = !isSaving,
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
            value = restaurantName,
            onValueChange = onRestaurantNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_name)) },
            isError = nameInvalid,
            supportingText = if (nameInvalid) {
                { Text(stringResource(Res.string.restaurant_home_restaurant_name_invalid)) }
            } else {
                null
            },
            enabled = !isSaving,
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
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_description)) },
            enabled = !isSaving,
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        )

        error?.let {
            Text(
                text = it.label(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus(force = true)
                onSaveClick()
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(
                    if (isSaving) {
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
private fun EditMainInfoContentPreview() {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant
        var restaurantName by remember { mutableStateOf(restaurant.name) }
        var description by remember { mutableStateOf(restaurant.description.orEmpty()) }

        EditMainInfoContent(
            restaurantName = restaurantName,
            description = description,
            restaurantImageUrl = restaurant.imageUrl.orEmpty(),
            onImageChange = {},
            onRestaurantNameChange = { restaurantName = it },
            onDescriptionChange = { description = it },
            onSaveClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
