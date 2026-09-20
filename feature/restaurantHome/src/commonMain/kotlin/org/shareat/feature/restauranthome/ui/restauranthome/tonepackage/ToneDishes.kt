package org.shareat.feature.restauranthome.ui.restauranthome.tonepackage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantImage
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun DishItem(dish: RestaurantDish, modifier: Modifier = Modifier) {
    val priceUnits = dish.priceMinorUnits / 100
    val priceCents = (dish.priceMinorUnits % 100).toString().padStart(2, '0')

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RestaurantImage(
            imageUrl = dish.imageUrl,
            contentDescription = dish.imageDescription,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            Text(
                text = dish.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = dish.description.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = "$priceUnits,$priceCents €",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            textAlign = TextAlign.End
        )
    }
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
