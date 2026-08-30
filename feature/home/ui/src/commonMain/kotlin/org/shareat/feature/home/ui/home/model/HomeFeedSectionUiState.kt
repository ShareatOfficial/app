package org.shareat.feature.home.ui.home.model

private const val HighlightsBatchSize = 4

sealed interface HomeFeedSectionUiState {
    data class Highlights(val restaurants: List<RestaurantCardUiState>) : HomeFeedSectionUiState
    data class Standalone(val restaurant: RestaurantCardUiState) : HomeFeedSectionUiState
}

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
