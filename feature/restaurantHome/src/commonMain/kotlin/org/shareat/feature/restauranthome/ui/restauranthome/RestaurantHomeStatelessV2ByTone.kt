package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_other
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_title
import shareat.feature.restauranthome.ui.generated.resources.restaurant_placeholder_hero

@Composable
internal fun RestaurantHomeStatelessV2ByTone() {
    //Three parts. Top Bar with the options that the restaurant have. Switch to customer preview. change layout type.
    /*
    What things I want to make the restaurant do?
    - View the menu as a customer
    - Create QR code to let the user navigate to the restaurant.
    - Switch between restaurant publish state. Only if it achieved the values. Name, description and more.
    - Change layout type. Out of MVP scope.
     */

    val uiState = RestaurantHomePreviewData.loaded
    val restaurant = (uiState.content as RestaurantHomeContent.Loaded).restaurant
    val isCustomerView = rememberSaveable { mutableStateOf(false) }

    RestaurantDishes(
        restaurant = restaurant,
        isCustomerView = isCustomerView.value,
        onCustomerViewChange = { isCustomerView.value = it },
        onOpenDirectionsClick = {},
        onRateClick = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun HomeRestaurantTopBar(
    title: String,
    restaurantName: String,
    isCustomerView: Boolean,
    onCustomerViewChange: (Boolean) -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                text = if (isCustomerView) "Editar" else "Cliente",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isCustomerView,
                onCheckedChange = onCustomerViewChange,
            )
        }
    }
}

@Composable
private fun RestaurantInfo(
    restaurant: RestaurantHomeData,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().background(color = MaterialTheme.colorScheme.background)
    ) {
        // Image + main info box with the image the restaurant choose behind.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 297.dp), // ~1/3 of a 891dp tall screen like Pixel 9

        ) {
            AsyncImage(
                model = restaurant.imageUrl?.ifEmpty { null }
                    ?: Res.drawable.restaurant_placeholder_hero,
                contentDescription = restaurant.imageDescription,
                modifier = Modifier.fillMaxWidth().matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = restaurant.description.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        // Secondary info.  Street, rating, if it is open, etc...
        Spacer(modifier = Modifier.height(16.dp))
        // TODO  el onclick cambiara dependiendo del state. si es customer vview o no. Pero eso lo gestionará el viewModel

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeRestaurantActionRow(
                icon = Icons.Outlined.LocationOn,
                text = restaurant.address.streetLine,
                onClick = onOpenDirectionsClick,
                trailingContent = {}
            )
            HomeRestaurantActionRow(
                icon = Icons.Filled.Star,
                text = restaurant.ratingLabel ?: "Sin calificación",
                onClick = onRateClick,
                trailingContent = {}
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun HomeRestaurantActionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(modifier = Modifier.padding(16.dp).clickable(onClick = onClick)) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        trailingContent()
    }
}

@Composable
private fun CategoriesRow(
    categories: List<DishCategory?>,
    selectedCategoryIndex: Int,
    categoryListState: LazyListState,
    onHeightChanged: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(it.height) },
    ) {
        LazyRow(
            state = categoryListState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
        ) {
            items(
                count = categories.size,
                key = { index -> categories[index]?.name ?: "other" },
            ) { index ->
                val category = categories[index]
                FilterChip(
                    selected = index == selectedCategoryIndex,
                    onClick = { onCategoryClick(index) },
                    label = {
                        Text(
                            text = category?.label()
                                ?: stringResource(Res.string.restaurant_home_category_other),
                        )
                    },
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RestaurantDishes(
    restaurant: RestaurantHomeData,
    isCustomerView: Boolean,
    onCustomerViewChange: (Boolean) -> Unit,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categorySections = remember(restaurant.dishes) {
        restaurant.dishes.groupBy { it.category }.entries.toList()
    }
    val categoryItemIndices = remember(categorySections) {
        // Restaurant info and the sticky navigation occupy the first two lazy-list items.
        var nextItemIndex = 2
        categorySections.map { section ->
            nextItemIndex.also {
                nextItemIndex += section.value.size + 1
            }
        }
    }
    val categories = remember(categorySections) {
        categorySections.map { it.key }
    }
    val categoryListState = rememberLazyListState()
    val dishListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val topBarHeightPx = remember { mutableIntStateOf(0) }
    val categoryBarHeightPx = remember { mutableIntStateOf(0) }
    val categoryBarTranslationPx = remember(dishListState, topBarHeightPx) {
        derivedStateOf {
            val categoryBarOffsetPx = dishListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == "category-navigation" }
                ?.offset
                ?: return@derivedStateOf 0

            (topBarHeightPx.intValue - categoryBarOffsetPx)
                .coerceIn(0, topBarHeightPx.intValue)
        }
    }
    val pinnedNavigationHeightPx = topBarHeightPx.intValue + categoryBarHeightPx.intValue
    val selectedCategoryIndex = remember(
        dishListState,
        categoryItemIndices,
        pinnedNavigationHeightPx,
    ) {
        derivedStateOf {
            val firstContentItemIndex = dishListState.layoutInfo.visibleItemsInfo
                .firstOrNull { item ->
                    item.index >= (categoryItemIndices.firstOrNull() ?: Int.MAX_VALUE) &&
                            item.offset + item.size > pinnedNavigationHeightPx
                }
                ?.index
                ?: dishListState.firstVisibleItemIndex

            when {
                categoryItemIndices.isEmpty() -> 0
                dishListState.layoutInfo.totalItemsCount == 0 -> 0
                !dishListState.canScrollForward && dishListState.firstVisibleItemIndex > 0 ->
                    categoryItemIndices.lastIndex

                else -> categoryItemIndices
                    .indexOfLast { it <= firstContentItemIndex }
                    .coerceAtLeast(0)
            }
        }
    }.value

    LaunchedEffect(selectedCategoryIndex) {
        if (categorySections.isNotEmpty()) {
            categoryListState.animateScrollToItem(selectedCategoryIndex)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = dishListState,
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(key = "restaurant-info") {
                RestaurantInfo(
                    restaurant = restaurant,
                    onOpenDirectionsClick = onOpenDirectionsClick,
                    onRateClick = onRateClick,
                )
            }

            stickyHeader(key = "category-navigation") {
                CategoriesRow(
                    categories = categories,
                    selectedCategoryIndex = selectedCategoryIndex,
                    categoryListState = categoryListState,
                    onHeightChanged = { categoryBarHeightPx.intValue = it },
                    onCategoryClick = { index ->
                        coroutineScope.launch {
                            dishListState.animateScrollToItem(
                                index = categoryItemIndices[index],
                                scrollOffset = -pinnedNavigationHeightPx,
                            )
                        }
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = categoryBarTranslationPx.value.toFloat()
                        }
                        .zIndex(1f),
                )
            }

            categorySections.forEach { (category, dishesList) ->
                item(key = "header-${category?.name ?: "other"}") {
                    Text(
                        text = category?.label()
                            ?: stringResource(Res.string.restaurant_home_category_other),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(
                    count = dishesList.size,
                    key = { index -> dishesList[index].id },
                ) { index ->
                    DishItem(
                        dish = dishesList[index],
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { /* TODO */ },
                    )
                }
            }
        }

        HomeRestaurantTopBar(
            title = stringResource(Res.string.restaurant_home_title),
            isCustomerView = isCustomerView,
            onCustomerViewChange = onCustomerViewChange,
            restaurantName = restaurant.name,
            scrollState = dishListState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .onSizeChanged { topBarHeightPx.intValue = it.height },
        )
    }
}

@Composable
private fun DishItem(dish: RestaurantDish, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = dish.imageUrl?.ifEmpty { null },
            contentDescription = dish.imageDescription,
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
            text = "$ ${dish.priceMinorUnits}",
            style = MaterialTheme.typography.titleMedium,
            //modifier = Modifier.weight(1f),
            maxLines = 1,
            textAlign = TextAlign.End
        )
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

@FormFactorPreviews
@Composable
private fun RestaurantHomeStatelessV2ByTonePreview() {
    ShareatTheme {
        RestaurantHomeStatelessV2ByTone()
    }
}

@Preview
@Composable
private fun DishItemPreview() {
    ShareatTheme {
        val uiState = RestaurantHomePreviewData.loaded
        val restaurant = (uiState.content as RestaurantHomeContent.Loaded).restaurant
        DishItem(
            dish = restaurant.dishes.first()
                .copy(imageUrl = "https://imgs.search.brave.com/jI7tFJqoulIUMoKkI4w0xw_JiOevDaE-FTkwxJgj61Y/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly9tZWRp/YS5nZXR0eWltYWdl/cy5jb20vaWQvOTgw/ODcwMjIvZXMvZm90/by9hbi1leGhpYml0/b3Itc2VydmVzLWtl/YmFiLW1lYXQtZnJv/bS1hLW5ldy1kb25l/ci1rZWJhYi1jdXR0/aW5nLXJvYm90LWR1/cmluZy10aGUtZG9n/YS10aGUtZG9uZXIu/anBnP3M9NjEyeDYx/MiZ3PTAmaz0yMCZj/PTNLSnpSMGVlRVhJ/RTcxMmc0WEpYbm5L/b3o1WXpPb1VfZHo3/S05wcXNFV1U9")
        )
    }
}
