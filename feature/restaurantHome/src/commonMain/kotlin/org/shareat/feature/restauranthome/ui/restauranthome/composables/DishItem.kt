package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.shared.designsystem.components.restaurant.RestaurantDishRow
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun DishItem(dish: RestaurantDish, modifier: Modifier = Modifier) {
    val priceUnits = dish.priceMinorUnits / 100
    val priceCents = (dish.priceMinorUnits % 100).toString().padStart(2, '0')
    RestaurantDishRow(
        name = dish.name,
        description = dish.description.orEmpty(),
        priceLabel = "$priceUnits,$priceCents €",
        modifier = modifier,
        image = { imageModifier ->
            RestaurantImage(
                imageUrl = dish.imageUrl,
                contentDescription = dish.imageDescription,
                modifier = imageModifier,
                contentScale = ContentScale.Crop,
            )
        },
    )
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun DishItemPreview() {
    ShareatTheme {
        val restaurant =
            (RestaurantHomePreviewData.loaded.content as RestaurantHomeContent.Loaded).restaurant

        DishItem(
            dish = restaurant.dishes.first(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
