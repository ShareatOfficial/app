package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_management
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_preview
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_switch_to_management
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_switch_to_preview
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_title

@Composable
internal fun CustomerModeTopBar(mode: RestaurantHomeMode, onModeToggle: () -> Unit) {
    val isManagement = mode == RestaurantHomeMode.MANAGEMENT
    val actionDescription = stringResource(
        if (isManagement) Res.string.restaurant_home_switch_to_preview
        else Res.string.restaurant_home_switch_to_management,
    )
    TopAppBar(
        title = {
            Text(
                text = "${stringResource(Res.string.restaurant_home_title)} · " + stringResource(
                    if (isManagement) Res.string.restaurant_home_management else Res.string.restaurant_home_preview,
                ),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        actions = {
            IconButton(
                onClick = onModeToggle,
                modifier = Modifier.semantics { role = Role.Button },
            ) {
                Icon(
                    imageVector = if (isManagement) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = actionDescription,
                )
            }
        },
    )
}
