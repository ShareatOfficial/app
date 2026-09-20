package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_add_dish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_other

/** Adds the management action and every category section to the parent restaurant LazyColumn. */
internal fun LazyListScope.restaurantDishListContent(
    categorySections: List<Map.Entry<DishCategory?, List<RestaurantDish>>>,
    isEditMode: Boolean,
    onAddDishClick: () -> Unit,
    onDishClick: (RestaurantDish) -> Unit,
) {
    // Keep this item in the lazy list while hidden so AnimatedVisibility can finish its exit.
    item(key = "add-dish") {
        AnimatedAddDishButton(
            visible = isEditMode,
            onClick = onAddDishClick,
        )
    }

    categorySections.forEach { (category, dishes) ->
        item(key = "header-${category?.name ?: "other"}") {
            DishCategoryHeader(category = category)
        }
        items(
            items = dishes,
            key = RestaurantDish::id,
        ) { dish ->
            DishItem(
                dish = dish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onDishClick(dish) },
            )
        }
    }
}

@Composable
private fun AnimatedAddDishButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing,
            ),
            initialOffsetY = { fullHeight -> -fullHeight },
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 180,
                easing = FastOutLinearInEasing,
            ),
            targetOffsetY = { fullHeight -> -fullHeight },
        ),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Text(
                    text = stringResource(Res.string.restaurant_home_add_dish),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DishCategoryHeader(category: DishCategory?) {
    Text(
        text = category?.label()
            ?: stringResource(Res.string.restaurant_home_category_other),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
