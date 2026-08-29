package org.shareat.feature.home.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.app.domain.model.RestaurantId
import org.shareat.feature.home.ui.home.composables.HomeSearchBar
import org.shareat.feature.home.ui.home.composables.RestaurantCard
import shareat.feature.home.ui.generated.resources.Res
import shareat.feature.home.ui.generated.resources.recommended

private const val RestaurantGridColumns = 2
private val HighlightsSectionShape = RoundedCornerShape(24.dp)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenStateless(
        uiState = uiState,
        modifier = modifier,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRetryClick = viewModel::onRetryClick,
    )
}

@Composable
private fun HomeScreenStateless(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onSearchQueryChanged: (String) -> Unit = {},
    onRetryClick: () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChanged,
                modifier = Modifier.padding(16.dp),
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val content = uiState.content) {
                    HomeContentUiState.Loading ->
                        HomeLoading(modifier = Modifier.align(Alignment.Center))

                    is HomeContentUiState.Error -> HomeError(
                        message = content.message,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    is HomeContentUiState.Loaded -> if (content.sections.isEmpty()) {
                        HomeEmpty(modifier = Modifier.align(Alignment.Center))
                    } else {
                        HomeFeed(
                            sections = content.sections,
                            showRecommendedTitle = uiState.searchQuery.isBlank(),
                            modifier = Modifier.align(Alignment.TopStart),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFeed(
    sections: List<HomeFeedSectionUiState>,
    showRecommendedTitle: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showRecommendedTitle) {
            item {
                Text(
                    text = stringResource(Res.string.recommended),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        itemsIndexed(sections, key = { index, _ -> index }) { _, section ->
            when (section) {
                is HomeFeedSectionUiState.Highlights -> RestaurantHighlightsSection(
                    restaurants = section.restaurants,
                )

                is HomeFeedSectionUiState.Standalone -> RestaurantStandaloneCard(
                    restaurant = section.restaurant,
                )
            }
        }
    }
}

@Composable
private fun RestaurantHighlightsSection(restaurants: List<RestaurantCardUiState>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(HighlightsSectionShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            restaurants.chunked(RestaurantGridColumns).forEach { rowRestaurants ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    rowRestaurants.forEach { restaurant ->
                        RestaurantCard(
                            name = restaurant.name,
                            heroImageUrl = restaurant.heroImageUrl,
                            heroImageDescription = restaurant.heroImageDescription,
                            ratingLabel = restaurant.ratingLabel,
                            isOpen = restaurant.isOpen,
                            address = restaurant.address,
                            dishReviews = restaurant.dishReviews,
                            showDishReviews = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantStandaloneCard(restaurant: RestaurantCardUiState) {
    RestaurantCard(
        name = restaurant.name,
        heroImageUrl = restaurant.heroImageUrl,
        heroImageDescription = restaurant.heroImageDescription,
        ratingLabel = restaurant.ratingLabel,
        isOpen = restaurant.isOpen,
        address = restaurant.address,
        dishReviews = restaurant.dishReviews,
        showDishReviews = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
}

@Composable
private fun HomeLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun HomeEmpty(modifier: Modifier = Modifier) {
    Text(
        text = "No restaurants found.",
        modifier = modifier.padding(24.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun HomeError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetryClick) {
            Text("Retry")
        }
    }
}

private object HomePreviewData {
    val dishReviews = listOf(
        DishReviewUiState(
            dishName = "Pulpo a la brasa",
            comment = "Tiernísimo y con el punto justo de humo.",
            rating = 5,
        ),
        DishReviewUiState(
            dishName = "Croquetas de jamón ibérico",
            comment = "Cremosas y recién hechas.",
            rating = 4,
        ),
    )

    // 6 restaurants renders a reviews-free Highlights section (4) followed by 2 standalone
    // full-width cards with reviews, exercising every branch of toFeedSections().
    val restaurants = (0 until 6).map { index ->
        RestaurantCardUiState(
            id = RestaurantId("restaurant-$index"),
            name = "Casa Naranja ${index + 1}",
            heroImageUrl = "https://images.example.com/restaurants/casa-naranja.jpg",
            heroImageDescription = "Interior de Casa Naranja",
            ratingLabel = "4.8",
            isOpen = index % 3 != 0,
            address = "Calle del Olmo, 18, Madrid",
            dishReviews = if (index % 2 == 0) dishReviews else emptyList(),
        )
    }

    val loading = HomeUiState(content = HomeContentUiState.Loading)
    val content = HomeUiState(content = HomeContentUiState.Loaded(restaurants.toFeedSections()))
    val searching = HomeUiState(
        searchQuery = "naranja",
        content = HomeContentUiState.Loaded(restaurants.toFeedSections()),
    )
    val empty = HomeUiState(content = HomeContentUiState.Loaded(emptyList()))
    val error = HomeUiState(
        content = HomeContentUiState.Error("The service is temporarily unavailable."),
    )
}

@HomeFormFactorPreviews
@Composable
private fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreenStateless(uiState = HomePreviewData.loading)
    }
}

@HomeFormFactorPreviews
@Composable
private fun HomeScreenContentPreview() {
    MaterialTheme {
        HomeScreenStateless(uiState = HomePreviewData.content)
    }
}

@HomeFormFactorPreviews
@Composable
private fun HomeScreenSearchingPreview() {
    MaterialTheme {
        HomeScreenStateless(uiState = HomePreviewData.searching)
    }
}

@HomeFormFactorPreviews
@Composable
private fun HomeScreenEmptyPreview() {
    MaterialTheme {
        HomeScreenStateless(uiState = HomePreviewData.empty)
    }
}

@HomeFormFactorPreviews
@Composable
private fun HomeScreenErrorPreview() {
    MaterialTheme {
        HomeScreenStateless(uiState = HomePreviewData.error)
    }
}
