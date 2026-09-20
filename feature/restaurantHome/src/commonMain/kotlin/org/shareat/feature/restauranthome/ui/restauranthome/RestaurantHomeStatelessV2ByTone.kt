package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.restauranthome.composables.ErrorContent
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantHomeEmptyContent
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantHomeSkeleton
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.feature.restauranthome.ui.restauranthome.tonepackage.CategoriesRow
import org.shareat.feature.restauranthome.ui.restauranthome.tonepackage.DishItem
import org.shareat.feature.restauranthome.ui.restauranthome.tonepackage.HomeRestaurantTopBar
import org.shareat.feature.restauranthome.ui.restauranthome.tonepackage.RestaurantHomeSheetContent
import org.shareat.feature.restauranthome.ui.restauranthome.tonepackage.RestaurantInfo
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_other
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_title

//Three parts. Top Bar with the options that the restaurant have. Switch to customer preview. change layout type.
/*
What things I want to make the restaurant do?
- View the menu as a customer
- Create QR code to let the user navigate to the restaurant.
- Switch between restaurant publish state. Only if it achieved the values. Name, description and more.
- Change layout type. Out of MVP scope.
 */
// TODO I forgot to add fields as restaurant number. Do not implement it for now.
@Composable
internal fun RestaurantHomeV2ByTone(
    modifier: Modifier = Modifier,
    onRestaurantImageChange: () -> Unit = {},
    onDishImageChange: () -> Unit = {},
    onDishReviewClick: ((String) -> Unit)? = null,
    viewModel: RestaurantHomeViewModelV2ByTone = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RestaurantHomeStatelessV2ByTone(
        uiState = uiState,
        modifier = modifier,
        onRetryClick = viewModel::onRetryClick,
        onEditModeChange = viewModel::onEditModeChange,
        onMainInfoClick = viewModel::onMainInfoClick,
        onAddressClick = viewModel::onAddressClick,
        onRatingClick = viewModel::onRatingClick,
        onCategoriesEditClick = viewModel::onCategoriesEditClick,
        onDishClick = viewModel::onDishClick,
        onDismissBottomSheet = viewModel::onDismissBottomSheet,
        onRestaurantNameChange = viewModel::onRestaurantNameChange,
        onRestaurantDescriptionChange = viewModel::onRestaurantDescriptionChange,
        onRestaurantImageChange = onRestaurantImageChange,
        onDishImageChange = onDishImageChange,
        onDishNameChange = viewModel::onDishNameChange,
        onDishDescriptionChange = viewModel::onDishDescriptionChange,
        onDishPriceChange = viewModel::onDishPriceChange,
        onDishAllergenClick = viewModel::onDishAllergenClick,
        onAddressStreetLineChange = viewModel::onAddressStreetLineChange,
        onAddressLocalityChange = viewModel::onAddressLocalityChange,
        onAddressPostalCodeChange = viewModel::onAddressPostalCodeChange,
        onAddressRegionChange = viewModel::onAddressRegionChange,
        onCategoryClick = viewModel::onCategoryClick,
        onDishReviewClick = onDishReviewClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantHomeStatelessV2ByTone(
    uiState: RestaurantHomeUiStateByTone,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
    onEditModeChange: (Boolean) -> Unit = {},
    onMainInfoClick: () -> Unit = {},
    onAddressClick: () -> Unit = {},
    onRatingClick: () -> Unit = {},
    onCategoriesEditClick: () -> Unit = {},
    onDishClick: (String) -> Unit = {},
    onDismissBottomSheet: () -> Unit = {},
    onRestaurantNameChange: (String) -> Unit = {},
    onRestaurantDescriptionChange: (String) -> Unit = {},
    onRestaurantImageChange: () -> Unit = {},
    onDishImageChange: () -> Unit = {},
    onDishNameChange: (String) -> Unit = {},
    onDishDescriptionChange: (String) -> Unit = {},
    onDishPriceChange: (String) -> Unit = {},
    onDishAllergenClick: (EuAllergen) -> Unit = {},
    onAddressStreetLineChange: (String) -> Unit = {},
    onAddressLocalityChange: (String) -> Unit = {},
    onAddressPostalCodeChange: (String) -> Unit = {},
    onAddressRegionChange: (String) -> Unit = {},
    onCategoryClick: (DishCategory) -> Unit = {},
    onDishReviewClick: ((String) -> Unit)? = null,
) {
    when (val content = uiState.content) {
        RestaurantHomeContent.Loading -> RestaurantHomeSkeleton(modifier = modifier)
        is RestaurantHomeContent.Error -> ErrorContent(
            error = content.error,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )

        RestaurantHomeContent.Empty -> RestaurantHomeEmptyContent(modifier = modifier)
        is RestaurantHomeContent.Loaded -> RestaurantHomeCompact(
            restaurant = content.restaurant,
            isEditMode = uiState.isEditMode,
            onEditModeChange = onEditModeChange,
            onOpenDirectionsClick = onAddressClick,
            onRateClick = onRatingClick,
            onEditMainInfoClick = onMainInfoClick,
            onEditCategoryClick = onCategoriesEditClick,
            onDishClick = { onDishClick(it.id) },
            modifier = modifier.fillMaxSize(),
        )
    }

    val activeBottomSheet = uiState.activeBottomSheet
    val restaurant = (uiState.content as? RestaurantHomeContent.Loaded)?.restaurant
    // TODO for web ideally we have here a check and if the screen is Desktop size we use a Side Sheet. Do not implement for the moment
    if (activeBottomSheet != null && restaurant != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissBottomSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        ) {
            RestaurantHomeSheetContent(
                sheet = activeBottomSheet,
                uiState = uiState,
                restaurant = restaurant,
                onRestaurantNameChange = onRestaurantNameChange,
                onRestaurantDescriptionChange = onRestaurantDescriptionChange,
                onRestaurantImageChange = onRestaurantImageChange,
                onDishImageChange = onDishImageChange,
                onDishNameChange = onDishNameChange,
                onDishDescriptionChange = onDishDescriptionChange,
                onDishPriceChange = onDishPriceChange,
                onDishAllergenClick = onDishAllergenClick,
                onAddressStreetLineChange = onAddressStreetLineChange,
                onAddressLocalityChange = onAddressLocalityChange,
                onAddressPostalCodeChange = onAddressPostalCodeChange,
                onAddressRegionChange = onAddressRegionChange,
                onCategoryClick = onCategoryClick,
                onDishReviewClick = onDishReviewClick,
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RestaurantHomeCompact(
    restaurant: RestaurantHomeData,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit,
    onEditMainInfoClick: () -> Unit,
    onEditCategoryClick: () -> Unit,
    onDishClick: (RestaurantDish) -> Unit,
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
                    onEditMainInfoClick = onEditMainInfoClick,
                    isEditMode = isEditMode,
                )
            }

            stickyHeader(key = "category-navigation") {
                CategoriesRow(
                    categories = restaurant.categories,
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
                    isEditMode = isEditMode,
                    onEditCategoryClick = onEditCategoryClick,
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
                            .clickable { onDishClick(dishesList[index]) },
                    )
                }
            }
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
private fun rememberCategoryItemWithIndices(categorySections: List<Map.Entry<DishCategory?, List<RestaurantDish>>>): List<Int> {
    return remember(categorySections) {
        // Restaurant info and the sticky navigation occupy the first two lazy-list items.
        var nextItemIndex = 2
        categorySections.map { section ->
            nextItemIndex.also {
                nextItemIndex += section.value.size + 1
            }
        }
    }
}

@Composable
private fun rememberSelectedCategoryIndex(
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


@FormFactorPreviews
@Composable
private fun RestaurantHomeStatelessV2ByTonePreview() {
    ShareatTheme {
        val content = RestaurantHomePreviewData.loaded.content
        val restaurant = (content as RestaurantHomeContent.Loaded).restaurant
        var uiState by remember {
            mutableStateOf(RestaurantHomeUiStateByTone(content = content))
        }

        RestaurantHomeStatelessV2ByTone(
            uiState = uiState,
            onEditModeChange = { isEditMode ->
                uiState = uiState.copy(
                    isEditMode = isEditMode,
                    activeBottomSheet = null,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onMainInfoClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.EDIT_MAIN_INFO,
                    mainInfoDraft = RestaurantMainInfoDraft(
                        name = restaurant.name,
                        description = restaurant.description.orEmpty(),
                        imageUrl = restaurant.imageUrl.orEmpty(),
                    ),
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onAddressClick = {
                val isEditMode = uiState.isEditMode
                uiState = uiState.copy(
                    activeBottomSheet = if (isEditMode) {
                        RestaurantHomeBottomSheet.EDIT_ADDRESS
                    } else {
                        RestaurantHomeBottomSheet.VIEW_ADDRESS
                    },
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = if (isEditMode) {
                        RestaurantAddressDraft(
                            streetLine = restaurant.address.streetLine,
                            locality = restaurant.address.locality,
                            postalCode = restaurant.address.postalCode,
                            region = restaurant.address.region.orEmpty(),
                            countryCode = restaurant.address.countryCode,
                        )
                    } else {
                        null
                    },
                    categoriesDraft = null,
                )
            },
            onRatingClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.VIEW_RATING,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onCategoriesEditClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.EDIT_CATEGORIES,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = restaurant.categories.toSet(),
                )
            },
            onDishClick = { dishId ->
                val dish = restaurant.dishes.firstOrNull { it.id == dishId }
                uiState = uiState.copy(
                    activeBottomSheet = if (uiState.isEditMode) {
                        RestaurantHomeBottomSheet.EDIT_DISH
                    } else {
                        RestaurantHomeBottomSheet.VIEW_DISH
                    },
                    selectedDishId = dishId,
                    dishEditForm = if (uiState.isEditMode && dish != null) {
                        DishEditFormUiState(
                            dishId = dish.id,
                            name = dish.name,
                            description = dish.description.orEmpty(),
                            price = dish.priceMinorUnits.toEditablePrice(),
                            allergens = dish.allergens,
                            imageUrl = dish.imageUrl,
                            isPublished = dish.isPublished,
                        )
                    } else {
                        null
                    },
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onDismissBottomSheet = {
                uiState = uiState.copy(
                    activeBottomSheet = null,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onRestaurantNameChange = { value ->
                uiState = uiState.copy(mainInfoDraft = uiState.mainInfoDraft?.copy(name = value))
            },
            onRestaurantDescriptionChange = { value ->
                uiState = uiState.copy(
                    mainInfoDraft = uiState.mainInfoDraft?.copy(description = value),
                )
            },
            onDishNameChange = { value ->
                uiState = uiState.copy(dishEditForm = uiState.dishEditForm?.copy(name = value))
            },
            onDishDescriptionChange = { value ->
                uiState = uiState.copy(
                    dishEditForm = uiState.dishEditForm?.copy(description = value),
                )
            },
            onDishPriceChange = { value ->
                uiState = uiState.copy(dishEditForm = uiState.dishEditForm?.copy(price = value))
            },
            onDishAllergenClick = { allergen ->
                val form = uiState.dishEditForm
                if (form != null) {
                    uiState = uiState.copy(
                        dishEditForm = form.copy(
                            allergens = if (allergen in form.allergens) {
                                form.allergens - allergen
                            } else {
                                form.allergens + allergen
                            },
                        ),
                    )
                }
            },
            onAddressStreetLineChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(streetLine = value),
                )
            },
            onAddressLocalityChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(locality = value),
                )
            },
            onAddressPostalCodeChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(postalCode = value),
                )
            },
            onAddressRegionChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(region = value),
                )
            },
            onCategoryClick = { category ->
                val categories = uiState.categoriesDraft
                if (categories != null) {
                    uiState = uiState.copy(
                        categoriesDraft = if (category in categories) {
                            categories - category
                        } else {
                            categories + category
                        },
                    )
                }
            },
        )
    }
}
