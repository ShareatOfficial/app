package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.feature.restaurant.ui.model.RestaurantHeaderUiState
import org.shareat.shared.designsystem.theme.ShareatTheme

private val BackdropHeight = 116.dp
private val BackdropTailWidth = 22.dp
private val BackdropTailHeight = 14.dp

@Composable
internal fun RestaurantHeaderBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val shape = remember(density) {
        val tailWidth = with(density) { BackdropTailWidth.toPx() }
        val tailHeight = with(density) { BackdropTailHeight.toPx() }
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width - tailWidth, size.height - tailHeight)
            lineTo(tailWidth, size.height - tailHeight)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BackdropHeight)
                .background(MaterialTheme.colorScheme.primary, shape),
        )
        content()
    }
}

@Preview
@Composable
private fun RestaurantHeaderBackdropPreview() {
    ShareatTheme {
        RestaurantHeaderBackdrop {
            RestaurantInfoCard(
                header = RestaurantHeaderUiState(
                    name = "The Rustic Spoon",
                    address = "Calle del Olmo, 18, Madrid",
                    description = "Texto descriptivo del restaurante con producto local.",
                    cuisineLabel = "Modern European",
                    priceRangeLabel = "$$ · Moderate",
                    isVerified = true,
                    ratingLabel = "4,8",
                    reviewCount = 1_284,
                ),
                onLeaveRateClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
