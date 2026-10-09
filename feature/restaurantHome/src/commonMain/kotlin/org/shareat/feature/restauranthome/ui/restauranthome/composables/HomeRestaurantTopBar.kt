package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.components.restaurant.RestaurantTopBar
import org.jetbrains.compose.resources.stringResource
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_management
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_preview

@Composable
internal fun HomeRestaurantTopBar(
    title: String,
    restaurantName: String,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
) {
    RestaurantTopBar(
        title = title,
        restaurantName = restaurantName,
        scrollState = scrollState,
        modifier = modifier,
        actions = {
            Row(
                modifier = Modifier.heightIn(min = 48.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (isEditMode) Res.string.restaurant_home_management
                        else Res.string.restaurant_home_preview,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Switch(checked = isEditMode, onCheckedChange = onEditModeChange)
            }
        },
    )
}
