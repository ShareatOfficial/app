package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.restauranthome.composables.CategoriesRow
import org.shareat.feature.restauranthome.ui.restauranthome.composables.HomeRestaurantTopBar
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantInfo
import org.shareat.feature.restauranthome.ui.restauranthome.composables.restaurantDishListContent
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_title

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun RestaurantHomeCompact(
    restaurant: RestaurantHomeData,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    isPublicationUpdating: Boolean,
    publicationError: RestaurantHomeError?,
    onPublicationStateChange: (Boolean) -> Unit,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit,
    onEditMainInfoClick: () -> Unit,
    onEditCategoryClick: () -> Unit,
    onDishClick: (RestaurantDish) -> Unit,
    onAddDishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categorySections = remember(restaurant.dishes) {
        restaurant.dishes.groupBy { it.category }.entries.toList()
    }
    val categoryItemIndices = rememberCategoryItemWithIndices(categorySections)
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
    val selectedCategoryIndex =
        rememberSelectedCategoryIndex(dishListState, categoryItemIndices, pinnedNavigationHeightPx)

    LaunchedEffect(selectedCategoryIndex) {
        if (categorySections.isNotEmpty()) {
            categoryListState.animateScrollToItem(selectedCategoryIndex)
        }
    }

    Box(modifier = modifier.background(color = MaterialTheme.colorScheme.background)) {
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
                    onEditMainInfoClick = onEditMainInfoClick,
                    isEditMode = isEditMode,
                    isPublicationUpdating = isPublicationUpdating,
                    publicationError = publicationError,
                    onPublicationStateChange = onPublicationStateChange,
                )
            }

            stickyHeader(key = "category-navigation") {
                CategoriesRow(
                    categories = restaurant.categories,
                    selectedCategoryIndex = selectedCategoryIndex,
                    categoryListState = categoryListState,
                    onHeightChanged = { categoryBarHeightPx.intValue = it },
                    onCategoryClick = { index ->
                        categoryItemIndices.getOrNull(index)?.let { itemIndex ->
                            coroutineScope.launch {
                                dishListState.animateScrollToItem(
                                    index = itemIndex,
                                    scrollOffset = -pinnedNavigationHeightPx,
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = categoryBarTranslationPx.value.toFloat()
                        }
                        .zIndex(1f),
                    isEditMode = isEditMode,
                    onEditCategoryClick = onEditCategoryClick,
                )
            }

            restaurantDishListContent(
                categorySections = categorySections,
                isEditMode = isEditMode,
                onAddDishClick = onAddDishClick,
                onDishClick = onDishClick,
            )
        }

        HomeRestaurantTopBar(
            title = stringResource(Res.string.restaurant_home_title),
            isEditMode = isEditMode,
            onEditModeChange = onEditModeChange,
            restaurantName = restaurant.name,
            scrollState = dishListState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .onSizeChanged {
                    topBarHeightPx.intValue = it.height
                }, // FIX this what make the height change y tal quiero menos height
        )
    }
}


@Composable
internal fun rememberCategoryItemWithIndices(
    categorySections: List<Map.Entry<DishCategory?, List<RestaurantDish>>>,
): List<Int> {
    return remember(categorySections) {
        // Restaurant info, sticky navigation and the animated add action occupy three items.
        var nextItemIndex = 3
        categorySections.map { section ->
            nextItemIndex.also {
                nextItemIndex += section.value.size + 1
            }
        }
    }
}

@Composable
internal fun rememberSelectedCategoryIndex(
    dishListState: LazyListState,
    categoryItemIndices: List<Int>,
    pinnedNavigationHeightPx: Int
): Int {
    return remember(
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
}
