package org.shareat.feature.home.ui.home

import org.shareat.app.domain.model.RestaurantId

private const val HighlightsBatchSize = 4

data class HomeUiState(
    val searchQuery: String = "",
    val content: HomeContentUiState = HomeContentUiState.Loading,
)

sealed interface HomeContentUiState {
    data object Loading : HomeContentUiState
    data class Loaded(val sections: List<HomeFeedSectionUiState>) : HomeContentUiState
    data class Error(val message: String) : HomeContentUiState
}

sealed interface HomeFeedSectionUiState {
    data class Highlights(val restaurants: List<RestaurantCardUiState>) : HomeFeedSectionUiState
    data class Standalone(val restaurant: RestaurantCardUiState) : HomeFeedSectionUiState
}

data class RestaurantCardUiState(
    val id: RestaurantId,
    val name: String,
    val heroImageUrl: String?,
    val heroImageDescription: String?,
    val ratingLabel: String,
    val dishReviews: List<DishReviewUiState>,
)

data class DishReviewUiState(
    val dishName: String,
    val comment: String,
    val rating: Int,
)

/**
 * The first up to 4 restaurants become a reviews-free Highlights section; every restaurant after
 * that becomes its own full-width Standalone section with dish reviews shown.
 */
fun List<RestaurantCardUiState>.toFeedSections(): List<HomeFeedSectionUiState> {
    if (isEmpty()) return emptyList()
    val sections = mutableListOf<HomeFeedSectionUiState>(
        HomeFeedSectionUiState.Highlights(take(HighlightsBatchSize)),
    )
    drop(HighlightsBatchSize).forEach { restaurant ->
        sections += HomeFeedSectionUiState.Standalone(restaurant)
    }
    return sections
}
