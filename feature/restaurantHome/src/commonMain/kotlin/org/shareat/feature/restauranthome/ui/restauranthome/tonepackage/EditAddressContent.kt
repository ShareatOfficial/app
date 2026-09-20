package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantAddressDraft
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit_address
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_locality
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_postal_code
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_region
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_street

@Composable
internal fun EditAddressContent(
    form: RestaurantAddressDraft,
    onStreetLineChange: (String) -> Unit,
    onLocalityChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val nextAction = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Next) },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.restaurant_home_edit_address),
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = form.streetLine,
            onValueChange = onStreetLineChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_street)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = nextAction,
        )

        OutlinedTextField(
            value = form.locality,
            onValueChange = onLocalityChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_locality)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = nextAction,
        )

        OutlinedTextField(
            value = form.postalCode,
            onValueChange = onPostalCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_postal_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = nextAction,
        )

        OutlinedTextField(
            value = form.region,
            onValueChange = onRegionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.restaurant_home_region)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
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
private fun EditAddressContentPreview() {
    ShareatTheme {
        var form by remember {
            mutableStateOf(
                RestaurantAddressDraft(
                    streetLine = "Mar del Este, Muelle 3",
                    locality = "Sambas",
                    postalCode = "00003",
                    region = "East Blue",
                    countryCode = "OP",
                ),
            )
        }

        EditAddressContent(
            form = form,
            onStreetLineChange = { form = form.copy(streetLine = it) },
            onLocalityChange = { form = form.copy(locality = it) },
            onPostalCodeChange = { form = form.copy(postalCode = it) },
            onRegionChange = { form = form.copy(region = it) },
            modifier = Modifier.padding(16.dp),
        )
    }
}
