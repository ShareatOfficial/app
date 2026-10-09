package org.shareat.feature.restaurant.ui.restaurant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import org.shareat.feature.restaurant.ui.model.RestaurantUiState
import org.shareat.feature.restaurant.ui.model.headerIndices
import org.shareat.feature.restaurant.ui.model.menuSections
import org.shareat.feature.restaurant.ui.navigation.RestaurantNavigation
import org.shareat.feature.restaurant.ui.restaurant.composables.AllergenFilter
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantPublicHeader
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantPublicSheet
import org.shareat.feature.restaurant.ui.restaurant.composables.label
import org.shareat.shared.designsystem.components.restaurant.RestaurantCategoryNavigation
import org.shareat.shared.designsystem.components.restaurant.RestaurantDishRow
import org.shareat.shared.designsystem.components.restaurant.RestaurantDishRowSkeleton
import org.shareat.shared.designsystem.components.restaurant.RestaurantHeaderSkeleton
import org.shareat.shared.designsystem.components.restaurant.RestaurantImage
import org.shareat.shared.designsystem.components.restaurant.RestaurantTopBar
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_back
import shareat.feature.restaurant.ui.generated.resources.restaurant_category_other
import shareat.feature.restaurant.ui.generated.resources.restaurant_no_dishes_for_filters
import shareat.feature.restaurant.ui.generated.resources.restaurant_no_menus
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_blocked
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_reported
import shareat.feature.restaurant.ui.generated.resources.restaurant_title

private val ScreenPadding = 16.dp
private const val LoadingDishSkeletons = 3
// Header, allergen controls and sticky category navigation precede the section headings.
private const val MenuLeadingItems = 3

@Composable
fun RestaurantScreen(
    args: RestaurantArgs,
    modifier: Modifier = Modifier,
    navigation: RestaurantNavigation = koinInject(),
    onDishReviewRequest: (DishId, Int) -> Unit = { _, _ -> },
    reviewSubmissionCount: Int = 0,
    viewModel: RestaurantViewModel = koinViewModel(
        key = args.id,
        parameters = { parametersOf(args) },
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val reportedMessage = stringResource(Res.string.restaurant_review_reported)
    val blockedMessage = stringResource(Res.string.restaurant_review_blocked)
    LaunchedEffect(viewModel, reportedMessage, blockedMessage) {
        viewModel.moderationEvents.collect { feedback ->
            snackbarHostState.showSnackbar(when (feedback) {
                ReviewModerationFeedback.Reported -> reportedMessage
                ReviewModerationFeedback.Blocked -> blockedMessage
            })
        }
    }
    LaunchedEffect(reviewSubmissionCount) {
        if (reviewSubmissionCount > 0) viewModel.onRefresh()
    }

    RestaurantScreenStateless(
        uiState = uiState,
        modifier = modifier,
        onBackClick = navigation::goBack,
        onRefresh = viewModel::onRefresh,
        onErrorShown = viewModel::onErrorShown,
        onAllergenClick = viewModel::onAllergenClick,
        onDishRatingClick = { dishId, rating ->
            viewModel.onDishRatingClick(dishId, rating)
            onDishReviewRequest(DishId(dishId), rating)
        },
        onReportReview = viewModel::onReportReview,
        onBlockReviewer = viewModel::onBlockReviewer,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun RestaurantScreenStateless(
    uiState: RestaurantUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onErrorShown: () -> Unit = {},
    onAllergenClick: (EuAllergen) -> Unit = {},
    onDishRatingClick: (String, Int) -> Unit = { _, _ -> },
    onReportReview: (String, ReviewReportReason) -> Unit = { _, _ -> },
    onBlockReviewer: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
) {
    val hostState = snackbarHostState ?: remember { SnackbarHostState() }
    val errorMessage = uiState.error?.label()
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            hostState.showSnackbar(message)
            onErrorShown()
        }
    }
    val listState = rememberLazyListState()
    val categoryListState = rememberLazyListState()
    val sections = remember(uiState.dishes) { uiState.dishes.menuSections() }
    val headerIndices = remember(sections) { sections.headerIndices(MenuLeadingItems) }
    val categoryLabels = sections.map { section ->
        section.category?.label() ?: stringResource(Res.string.restaurant_category_other)
    }
    var topBarHeight by remember { mutableIntStateOf(0) }
    var categoryBarHeight by remember { mutableIntStateOf(0) }
    val pinnedHeight = topBarHeight + categoryBarHeight
    val selectedCategory by remember(listState, headerIndices, pinnedHeight) {
        derivedStateOf {
            val firstContentItem = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index >= (headerIndices.firstOrNull() ?: Int.MAX_VALUE) && it.offset + it.size > pinnedHeight
            }
            when {
                headerIndices.isEmpty() -> 0
                !listState.canScrollForward && listState.firstVisibleItemIndex > 0 -> headerIndices.lastIndex
                else -> headerIndices.indexOfLast {
                    it <= (firstContentItem?.index ?: listState.firstVisibleItemIndex)
                }.coerceAtLeast(0)
            }
        }
    }
    val categoryTranslation by remember(listState, topBarHeight) {
        derivedStateOf {
            val offset = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "category-navigation" }?.offset
            if (offset == null) 0 else (topBarHeight - offset).coerceIn(0, topBarHeight)
        }
    }
    LaunchedEffect(selectedCategory, categoryLabels) {
        if (categoryLabels.isNotEmpty()) categoryListState.animateScrollToItem(selectedCategory)
    }
    val scope = rememberCoroutineScope()
    var selectedDishId by rememberSaveable { mutableStateOf<String?>(null) }
    var infoPanel by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedDish = uiState.dishes.firstOrNull { it.id == selectedDishId }
    LaunchedEffect(selectedDishId, selectedDish) {
        if (selectedDishId != null && selectedDish == null) selectedDishId = null
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isReviewOpening by remember { mutableStateOf(false) }
    val closePanel = { selectedDishId = null; infoPanel = null }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(hostState) },
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = ScreenPadding),
            ) {
                item(key = "restaurant-info") {
                    if (uiState.isRefreshing) RestaurantHeaderSkeleton()
                    else RestaurantPublicHeader(
                        header = uiState.header,
                        onAddressClick = { infoPanel = "address" },
                        onRatingClick = { infoPanel = "rating" },
                    )
                }
                item(key = "allergen-filter") {
                    if (!uiState.isRefreshing && !uiState.isLoadingDishes && uiState.hasPublishedMenu) {
                        AllergenFilter(
                            state = uiState.allergenFilter,
                            onAllergenClick = onAllergenClick,
                            modifier = Modifier.padding(horizontal = ScreenPadding),
                        )
                    }
                }
                when {
                    uiState.isRefreshing || uiState.isLoadingDishes -> {
                        items(LoadingDishSkeletons) { RestaurantDishRowSkeleton() }
                    }
                    sections.isEmpty() -> {
                        item(key = "empty-menu") {
                            Text(
                                stringResource(
                                    if (uiState.hasPublishedMenu) Res.string.restaurant_no_dishes_for_filters
                                    else Res.string.restaurant_no_menus,
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 24.dp),
                            )
                        }
                    }
                    else -> {
                        stickyHeader(key = "category-navigation") {
                            RestaurantCategoryNavigation(
                                labels = categoryLabels,
                                selectedIndex = selectedCategory,
                                listState = categoryListState,
                                onCategoryClick = { index ->
                                    headerIndices.getOrNull(index)?.let { itemIndex ->
                                        scope.launch {
                                            listState.animateScrollToItem(itemIndex, scrollOffset = -pinnedHeight)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .onSizeChanged { categoryBarHeight = it.height }
                                    .graphicsLayer { translationY = categoryTranslation.toFloat() }
                                    .zIndex(1f),
                            )
                        }
                        sections.forEachIndexed { index, section ->
                            item(key = section.key) {
                                Text(
                                    categoryLabels[index],
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp, bottom = 8.dp),
                                )
                            }
                            items(section.dishes, key = { "dish-${it.id}" }) { dish ->
                                RestaurantDishRow(
                                    name = dish.name,
                                    description = dish.description.orEmpty(),
                                    priceLabel = dish.priceLabel,
                                    image = { imageModifier ->
                                        RestaurantImage(
                                            imageUrl = dish.imageUrl,
                                            contentDescription = dish.imageDescription ?: dish.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = imageModifier,
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { selectedDishId = dish.id }
                                        .padding(horizontal = ScreenPadding, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
            RestaurantTopBar(
                title = stringResource(Res.string.restaurant_title),
                restaurantName = uiState.header.name,
                scrollState = listState,
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.restaurant_back))
                    }
                },
                stackOnNarrowWidth = false,
                modifier = Modifier.align(Alignment.TopCenter).zIndex(2f).onSizeChanged { topBarHeight = it.height },
            )
        }
    }
    if (selectedDish != null || infoPanel != null) {
        ModalBottomSheet(onDismissRequest = closePanel, sheetState = sheetState) {
            RestaurantPublicSheet(
                header = uiState.header,
                dish = selectedDish,
                showAddress = infoPanel == "address",
                onClose = closePanel,
                onRatingClick = { rating ->
                    if (!isReviewOpening) {
                        selectedDish?.let { dish ->
                            isReviewOpening = true
                            scope.launch {
                                try {
                                    sheetState.hide()
                                    closePanel()
                                    onDishRatingClick(dish.id, rating)
                                } finally {
                                    isReviewOpening = false
                                }
                            }
                        }
                    }
                },
                onReportReview = onReportReview,
                onBlockReviewer = onBlockReviewer,
            )
        }
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantScreenPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.loaded)
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantScreenRefreshingPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.refreshing)
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantScreenFilteredEmptyPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.filteredEmpty)
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantScreenWithoutMenuPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.withoutMenu)
    }
}
