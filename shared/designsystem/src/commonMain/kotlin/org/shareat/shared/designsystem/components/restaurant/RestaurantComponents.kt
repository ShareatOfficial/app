package org.shareat.shared.designsystem.components.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment as ComposeAlignment
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import coil3.compose.AsyncImage
import shareat.shared.designsystem.generated.resources.Res
import shareat.shared.designsystem.generated.resources.restaurant_placeholder_hero
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextOverflow
import org.shareat.shared.designsystem.layout.safeDrawingTopPadding
import org.shareat.shared.designsystem.shimmerEffect

@Composable
fun RestaurantHeader(
    name: String,
    description: String,
    image: @Composable (Modifier) -> Unit,
    heroActions: @Composable () -> Unit = {},
    info: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().heightIn(min = 297.dp)) {
            val topContentPadding = if (maxWidth < 360.dp) 112.dp else 64.dp
            image(Modifier.matchParentSize())
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.7f)))
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
                    .safeDrawingTopPadding()
                    .padding(top = topContentPadding, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(name, style = MaterialTheme.typography.displayMedium, color = Color.White)
                Text(description, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
            Box(Modifier.fillMaxWidth().align(Alignment.CenterEnd), contentAlignment = Alignment.CenterEnd) {
                heroActions()
            }
        }
        Spacer(Modifier.height(16.dp))
        info()
    }
}

@Composable
fun RestaurantInfoAction(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.heightIn(min = 48.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = text,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        trailingContent()
    }
}

@Composable
fun RestaurantImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: ComposeAlignment = ComposeAlignment.Center,
    colorFilter: ColorFilter? = null,
    placeholder: DrawableResource = Res.drawable.restaurant_placeholder_hero,
) {
    val fallback = painterResource(placeholder)
    AsyncImage(
        model = imageUrl?.ifBlank { null },
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = fallback,
        error = fallback,
        fallback = fallback,
        contentScale = contentScale,
        alignment = alignment,
        colorFilter = colorFilter,
    )
}

@Composable
fun RestaurantTopBar(
    title: String,
    restaurantName: String,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    stackOnNarrowWidth: Boolean = true,
) {
    val progress = rememberCollapseProgress(LocalDensity.current, scrollState)
    val rowColor = MaterialTheme.colorScheme.surfaceContainer
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().drawBehind { drawRect(rowColor, alpha = progress.value) }
            .safeDrawingTopPadding().padding(horizontal = 16.dp),
    ) {
        val narrow = stackOnNarrowWidth && maxWidth < 360.dp
        val titleChip: @Composable (Modifier) -> Unit = { chipModifier ->
            Box(chipModifier.heightIn(min = 48.dp)) {
                Box(
                    Modifier.align(Alignment.CenterStart).graphicsLayer { alpha = 1f - progress.value }
                        .background(rowColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleLargeEmphasized, color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    Modifier.align(Alignment.CenterStart).graphicsLayer { alpha = progress.value }
                        .background(rowColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(restaurantName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (narrow) {
            Column(modifier = Modifier.fillMaxWidth()) {
                titleChip(Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(contentAlignment = Alignment.CenterStart) { navigationIcon() }
                    Box(contentAlignment = Alignment.CenterEnd) { actions() }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) { navigationIcon() }
                titleChip(Modifier.weight(1f))
                Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterEnd) { actions() }
            }
        }
    }
}

@Composable
fun RestaurantCategoryNavigation(
    labels: List<String>,
    selectedIndex: Int,
    listState: LazyListState,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        ) {
            if (leadingContent != null) {
                item(key = "leading-content") { leadingContent() }
            }
            itemsIndexed(labels, key = { index, label -> "$index-$label" }) { index, label ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onCategoryClick(index) },
                    label = { Text(label) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun RestaurantDishRow(
    name: String,
    description: String,
    priceLabel: String,
    image: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))) { image(Modifier.fillMaxSize()) }
        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
            Text(
                priceLabel,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
fun RestaurantDishDetails(
    name: String,
    description: String,
    priceLabel: String,
    image: @Composable (Modifier) -> Unit,
    ratingContent: @Composable () -> Unit = {},
    allergenContent: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackDetails = maxWidth < 360.dp || LocalDensity.current.fontScale > 1.2f
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp))) {
                image(Modifier.fillMaxSize())
            }
            if (stackDetails) {
                Text(name, style = MaterialTheme.typography.headlineSmall)
                Column { ratingContent() }
                Text(
                    description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(priceLabel, style = MaterialTheme.typography.titleMedium)
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                    Column(horizontalAlignment = Alignment.End) { ratingContent() }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        description,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(priceLabel, style = MaterialTheme.typography.titleMedium)
                }
            }
            allergenContent()
            actions()
        }
    }
}

@Composable
fun RestaurantHeaderSkeleton(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(297.dp).shimmerEffect(color))
        Box(Modifier.fillMaxWidth().padding(16.dp).height(20.dp).shimmerEffect(color, RoundedCornerShape(4.dp)))
    }
}

@Composable
fun RestaurantDishRowSkeleton(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    Row(modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(96.dp).shimmerEffect(color, RoundedCornerShape(12.dp)))
        Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.7f).height(20.dp).shimmerEffect(color, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth().height(14.dp).shimmerEffect(color, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun rememberCollapseProgress(density: Density, state: LazyListState): State<Float> {
    val fadeDistance = with(density) { 96.dp.toPx() }
    return remember(state, fadeDistance) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / fadeDistance).coerceIn(0f, 1f)
        }
    }
}
