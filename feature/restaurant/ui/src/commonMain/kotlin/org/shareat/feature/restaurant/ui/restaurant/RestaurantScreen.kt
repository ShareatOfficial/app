package org.shareat.feature.restaurant.ui.restaurant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import org.shareat.feature.restaurant.ui.model.RestaurantUiState
import org.shareat.feature.restaurant.ui.navigation.RestaurantNavigation
import org.shareat.feature.restaurant.ui.restaurant.composables.AllergenFilter
import org.shareat.feature.restaurant.ui.restaurant.composables.CategoryChipsRow
import org.shareat.feature.restaurant.ui.restaurant.composables.dish.DishCard
import org.shareat.feature.restaurant.ui.restaurant.composables.dish.DishCardSkeleton
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantInfoCard
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantHeaderBackdrop
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantInfoCardSkeleton
import org.shareat.feature.restaurant.ui.restaurant.composables.RestaurantTopBar
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_no_dishes_for_filters
import shareat.feature.restaurant.ui.generated.resources.restaurant_no_menus
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_blocked
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_reported
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.feature.restaurant.ui.restaurant.composables.label

private val ScreenPadding = 16.dp
private val FilterSpacing = 4.dp
private const val LoadingDishSkeletons = 3

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
        onCategoryClick = viewModel::onCategoryClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantScreenStateless(
    uiState: RestaurantUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onErrorShown: () -> Unit = {},
    onCategoryClick: (DishCategory?) -> Unit = {},
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RestaurantTopBar(name = uiState.header.name, onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState) },
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(ScreenPadding),
            ) {
                item {
                    RestaurantHeaderBackdrop {
                        if (uiState.isRefreshing) {
                            RestaurantInfoCardSkeleton(modifier = Modifier.padding(ScreenPadding))
                        } else {
                            RestaurantInfoCard(
                                header = uiState.header,
                                modifier = Modifier.padding(ScreenPadding),
                            )
                        }
                    }
                }
                filterSection(uiState, onCategoryClick, onAllergenClick)
                dishSection(uiState, onDishRatingClick, onReportReview, onBlockReviewer)
            }
        }
    }
}

private fun LazyListScope.filterSection(
    uiState: RestaurantUiState,
    onCategoryClick: (DishCategory?) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
) {
    if (uiState.isRefreshing || uiState.isLoadingDishes || !uiState.hasPublishedMenu) return
    item {
        Column(verticalArrangement = Arrangement.spacedBy(FilterSpacing)) {
            CategoryChipsRow(
                categories = uiState.categories,
                onCategoryClick = onCategoryClick,
                contentPadding = PaddingValues(horizontal = ScreenPadding),
            )
            AllergenFilter(
                state = uiState.allergenFilter,
                onAllergenClick = onAllergenClick,
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
        }
    }
}

private fun LazyListScope.dishSection(
    uiState: RestaurantUiState,
    onDishRatingClick: (String, Int) -> Unit,
    onReportReview: (String, ReviewReportReason) -> Unit,
    onBlockReviewer: (String) -> Unit,
) {
    if (uiState.isRefreshing || uiState.isLoadingDishes) {
        items(LoadingDishSkeletons) {
            DishCardSkeleton(modifier = Modifier.padding(horizontal = ScreenPadding))
        }
        return
    }
    if (!uiState.hasVisibleDishes) {
        item {
            EmptyMessage(
                text = stringResource(
                    if (uiState.hasPublishedMenu) {
                        Res.string.restaurant_no_dishes_for_filters
                    } else {
                        Res.string.restaurant_no_menus
                    },
                ),
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
        }
        return
    }
    items(uiState.dishes, key = { it.id }) { dish ->
        DishCard(
            dish = dish,
            onRatingClick = { rating -> onDishRatingClick(dish.id, rating) },
            onReportReview = onReportReview,
            onBlockReviewer = onBlockReviewer,
            modifier = Modifier.padding(horizontal = ScreenPadding),
        )
    }
}

@Composable
private fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
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
