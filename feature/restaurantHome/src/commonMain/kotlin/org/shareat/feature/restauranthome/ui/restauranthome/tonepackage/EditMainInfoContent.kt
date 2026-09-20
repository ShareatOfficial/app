package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_change_image
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_description
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_name


@Composable
internal fun EditMainInfoContent(
    restaurantName: String,
    description: String,
    restaurantImageUrl: String,
    onImageChange: () -> Unit,
    onRestaurantNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val changeImageLabel = stringResource(Res.string.restaurant_home_change_image)

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
            modifier = Modifier.padding(16.dp),
        )
    }
}
