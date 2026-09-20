package org.shareat.feature.home.ui.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.usecase.GetRestaurantsUseCase
import org.shareat.app.domain.usecase.RestaurantSummary
import org.shareat.feature.home.ui.home.model.DishReviewUiState
import org.shareat.feature.home.ui.home.model.HomeContentUiState
import org.shareat.feature.home.ui.home.model.HomeError
import org.shareat.feature.home.ui.home.model.HomeUiState
import org.shareat.feature.home.ui.home.model.RestaurantCardUiState
import org.shareat.feature.home.ui.home.model.toFeedSections
import kotlin.time.Duration.Companion.milliseconds

private const val HomePageOffset = 0
private const val HomePageSize = 50
private const val SearchDebounceMillis = 300L

@OptIn(FlowPreview::class)
@Stable
@KoinViewModel
class HomeViewModel(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    private var loadedRestaurants: List<RestaurantSummary> = emptyList()
    private var restaurantCards: List<RestaurantCardUiState> = emptyList()

    init {
        loadHome()
        observeSearchQuery()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .drop(1)
                .debounce(SearchDebounceMillis.milliseconds)
                .collect { query -> _uiState.update { it.copy(content = filteredContent(query)) } }
        }
    }

    fun onRetryClick() {
        if (_uiState.value.content is HomeContentUiState.Error) loadHome()
    }

    private fun loadHome() {
        _uiState.update { it.copy(content = HomeContentUiState.Loading) }
        viewModelScope.launch {
            when (
                val result = getRestaurantsUseCase(page = HomePageOffset, numberOfRestaurants = HomePageSize)
            ) {
                is RepositoryResult.Success -> {
                    loadedRestaurants = result.value
                    restaurantCards = result.value.map { it.toCardUiState() }
                    _uiState.update { it.copy(content = filteredContent(it.searchQuery)) }
                }

                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(content = HomeContentUiState.Error(result.error.toHomeError()))
                }
            }
        }
    }

    fun restaurantFor(id: RestaurantId): RestaurantSummary? =
        loadedRestaurants.firstOrNull { it.restaurant.id == id }

    private fun filteredContent(query: String): HomeContentUiState.Loaded = HomeContentUiState.Loaded(
        sections = restaurantCards
            .filter { restaurant -> restaurant.name.contains(query, ignoreCase = true) }
            .toFeedSections(),
    )
}

private fun RestaurantSummary.toCardUiState(): RestaurantCardUiState = RestaurantCardUiState(
    id = restaurant.id,
    name = restaurant.name,
    heroImageUrl = restaurant.heroImage?.url,
    heroImageDescription = restaurant.heroImage?.alternativeText,
    ratingLabel = ratingSummary.averageTenths.toRatingLabel(),
    isOpen = isOpen,
    address = restaurant.address?.let { "${it.streetLine}, ${it.locality}" }.orEmpty(),
    dishReviews = dishHighlights.map { highlight ->
        DishReviewUiState(
            dishName = highlight.dish.name,
            comment = requireNotNull(highlight.review.comment),
            rating = highlight.review.rating.value,
        )
    },
)

private fun Int?.toRatingLabel(): String? {
    if (this == null) return null
    val whole = this / 10
    val decimal = this % 10
    return "$whole.$decimal"
}

private fun RepositoryError.toHomeError(): HomeError = when (this) {
    RepositoryError.InvalidCredentials -> HomeError.INVALID_CREDENTIALS
    RepositoryError.Offline -> HomeError.OFFLINE
    RepositoryError.Unauthenticated -> HomeError.UNAUTHENTICATED
    RepositoryError.Forbidden -> HomeError.FORBIDDEN
    is RepositoryError.Unavailable -> HomeError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.AlreadyExists -> HomeError.ALREADY_EXISTS
    is RepositoryError.NotFound -> HomeError.NOT_FOUND
    is RepositoryError.Conflict, is RepositoryError.Validation -> HomeError.UNKNOWN
}
