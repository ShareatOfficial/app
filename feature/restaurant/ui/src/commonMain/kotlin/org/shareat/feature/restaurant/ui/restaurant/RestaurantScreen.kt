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
import org.shareat.app.domain.model.EuAllergen
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

private val ScreenPadding = 16.dp
private val FilterSpacing = 4.dp
private const val RefreshingDishSkeletons = 3

@Composable
fun RestaurantScreen(
    args: RestaurantArgs,
    modifier: Modifier = Modifier,
    navigation: RestaurantNavigation = koinInject(),
    viewModel: RestaurantViewModel = koinViewModel(
        key = args.id,
        parameters = { parametersOf(args) },
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    RestaurantScreenStateless(
        uiState = uiState,
        modifier = modifier,
        onBackClick = navigation::goBack,
        onLeaveRateClick = { navigation.openRestaurantReviewForm(args.id) },
        onRefresh = viewModel::onRefresh,
        onErrorShown = viewModel::onErrorShown,
        onCategoryClick = viewModel::onCategoryClick,
        onAllergenFilterToggle = viewModel::onAllergenFilterToggle,
        onAllergenClick = viewModel::onAllergenClick,
        onDishClick = viewModel::onDishClick,
        onDishRatingClick = viewModel::onDishRatingClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantScreenStateless(
    uiState: RestaurantUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLeaveRateClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onErrorShown: () -> Unit = {},
    onCategoryClick: (DishCategory?) -> Unit = {},
    onAllergenFilterToggle: () -> Unit = {},
    onAllergenClick: (EuAllergen) -> Unit = {},
    onDishClick: (String) -> Unit = {},
    onDishRatingClick: (String, Int) -> Unit = { _, _ -> },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RestaurantTopBar(name = uiState.header.name, onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                onLeaveRateClick = onLeaveRateClick,
                                modifier = Modifier.padding(ScreenPadding),
                            )
                        }
                    }
                }
                filterSection(uiState, onCategoryClick, onAllergenFilterToggle, onAllergenClick)
                dishSection(uiState, onDishClick, onDishRatingClick)
            }
        }
    }
}

private fun LazyListScope.filterSection(
    uiState: RestaurantUiState,
    onCategoryClick: (DishCategory?) -> Unit,
    onAllergenFilterToggle: () -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
) {
    if (uiState.isRefreshing || !uiState.hasPublishedMenu) return
    item {
        Column(verticalArrangement = Arrangement.spacedBy(FilterSpacing)) {
            CategoryChipsRow(
                categories = uiState.categories,
                onCategoryClick = onCategoryClick,
                contentPadding = PaddingValues(horizontal = ScreenPadding),
            )
            AllergenFilter(
                state = uiState.allergenFilter,
                onToggleExpanded = onAllergenFilterToggle,
                onAllergenClick = onAllergenClick,
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
        }
    }
}

private fun LazyListScope.dishSection(
    uiState: RestaurantUiState,
    onDishClick: (String) -> Unit,
    onDishRatingClick: (String, Int) -> Unit,
) {
    if (uiState.isRefreshing) {
        items(RefreshingDishSkeletons) {
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
            onClick = { onDishClick(dish.id) },
            onRatingClick = { rating -> onDishRatingClick(dish.id, rating) },
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

@RestaurantFormFactorPreviews
@Composable
private fun RestaurantScreenPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.loaded)
    }
}

@RestaurantFormFactorPreviews
@Composable
private fun RestaurantScreenRefreshingPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.refreshing)
    }
}

@RestaurantFormFactorPreviews
@Composable
private fun RestaurantScreenFilteredEmptyPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.filteredEmpty)
    }
}

@RestaurantFormFactorPreviews
@Composable
private fun RestaurantScreenWithoutMenuPreview() {
    ShareatTheme {
        RestaurantScreenStateless(uiState = RestaurantPreviewData.withoutMenu)
    }
}
