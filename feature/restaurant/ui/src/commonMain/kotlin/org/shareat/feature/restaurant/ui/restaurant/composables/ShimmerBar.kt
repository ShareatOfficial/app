package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.shimmerEffect

@Composable
internal fun ShimmerBar(
    widthFraction: Float,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmerEffect(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp)),
    )
}
