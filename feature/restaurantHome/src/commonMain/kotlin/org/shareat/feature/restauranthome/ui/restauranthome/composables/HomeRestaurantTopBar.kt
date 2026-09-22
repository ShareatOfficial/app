package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeRestaurantTopBar(
    title: String,
    restaurantName: String,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
) {
    val collapseProgress = rememberCollapseProgress(LocalDensity.current, scrollState)
    val rowColor = MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = rowColor,
                    alpha = collapseProgress.value,
                )
            }
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .graphicsLayer {
                    alpha = 1f - collapseProgress.value
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .height(48.dp)
                .graphicsLayer {
                    alpha = 0f + collapseProgress.value
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = restaurantName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.bodySmall.fontSize,
                    maxFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                ),
                maxLines = 1,
            )
        }
        Row(
            modifier = Modifier
                .height(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                ).align(Alignment.CenterEnd)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "Editar" else "Cliente",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isEditMode,
                onCheckedChange = onEditModeChange,
            )
        }
    }
}

@Composable
private fun rememberCollapseProgress(
    localDensity: Density,
    scrollState: LazyListState,
): State<Float> {
    val fadeDistancePx = with(localDensity) {
        96.dp.toPx()
    }

    return remember(scrollState, fadeDistancePx) {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (
                        scrollState.firstVisibleItemScrollOffset.toFloat() /
                                fadeDistancePx
                        ).coerceIn(0f, 1f)
            }
        }
    }
}
