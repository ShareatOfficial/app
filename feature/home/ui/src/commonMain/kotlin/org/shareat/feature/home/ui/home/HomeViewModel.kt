package org.shareat.feature.home.ui.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.home.domain.GetHomeRestaurantsUseCase
import org.shareat.feature.home.domain.RestaurantWithHighlights

private const val HomePageOffset = 0
private const val HomePageSize = 50

@Stable
@KoinViewModel
class HomeViewModel(
    private val getHomeRestaurantsUseCase: GetHomeRestaurantsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadedRestaurants: List<RestaurantCardUiState> = emptyList()

    init {
        loadHome()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, content = filteredContent(query)) }
    }

    fun onRetryClick() {
        if (_uiState.value.content is HomeContentUiState.Error) loadHome()
    }

    private fun loadHome() {
        _uiState.update { it.copy(content = HomeContentUiState.Loading) }
        viewModelScope.launch {
            when (
                val result = getHomeRestaurantsUseCase(offset = HomePageOffset, limit = HomePageSize)
            ) {
                is RepositoryResult.Success -> {
                    loadedRestaurants = result.value.map { it.toCardUiState() }
                    _uiState.update { it.copy(content = filteredContent(it.searchQuery)) }
                }

                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(content = HomeContentUiState.Error(result.error.toUserMessage()))
                }
            }
        }
    }

    private fun filteredContent(query: String): HomeContentUiState.Loaded = HomeContentUiState.Loaded(
        sections = loadedRestaurants
            .filter { restaurant -> restaurant.name.contains(query, ignoreCase = true) }
            .toFeedSections(),
    )
}

private fun RestaurantWithHighlights.toCardUiState(): RestaurantCardUiState = RestaurantCardUiState(
    id = restaurant.id,
    name = restaurant.name,
    heroImageUrl = restaurant.heroImage?.url,
    heroImageDescription = restaurant.heroImage?.alternativeText,
    ratingLabel = ratingSummary.averageTenths.toRatingLabel(),
    dishReviews = dishHighlights.map { highlight ->
        DishReviewUiState(
            dishName = highlight.dish.name,
            comment = requireNotNull(highlight.review.comment),
            rating = highlight.review.rating.value,
        )
    },
)

private fun Int?.toRatingLabel(): String {
    if (this == null) return "New"
    val whole = this / 10
    val decimal = this % 10
    return "$whole.$decimal"
}

private fun RepositoryError.toUserMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "Your session credentials are no longer valid."
    RepositoryError.Offline -> "You appear to be offline. Try again when connected."
    RepositoryError.Unauthenticated -> "Your session has expired. Please sign in again."
    RepositoryError.Forbidden -> "This account is not allowed to perform that action."
    is RepositoryError.Unavailable -> "The service is temporarily unavailable."
    is RepositoryError.AlreadyExists -> "The $entity already exists."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "The requested $entity could not be found."
    is RepositoryError.Validation -> reason
}
