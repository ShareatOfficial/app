package org.shareat.shared.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

private const val ShimmerMinAlpha = 0.15f
private const val ShimmerMaxAlpha = 0.35f
private const val ShimmerDurationMillis = 700

/**
 * Fills this shape with a pulsing translucent [color], for placeholder boxes standing in
 * for content that hasn't loaded yet. Every skeleton across the app should use this instead
 * of animating its own alpha, so all loading states pulse at the same pace.
 */
fun Modifier.shimmerEffect(color: Color, shape: Shape = RectangleShape): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerEffect")
    val alpha by transition.animateFloat(
        initialValue = ShimmerMinAlpha,
        targetValue = ShimmerMaxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShimmerDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerEffectAlpha",
    )
    background(color = color.copy(alpha = alpha), shape = shape)
}
