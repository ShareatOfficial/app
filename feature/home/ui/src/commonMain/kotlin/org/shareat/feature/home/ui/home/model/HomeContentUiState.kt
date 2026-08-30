package org.shareat.feature.home.ui.home.model

sealed interface HomeContentUiState {
    data object Loading : HomeContentUiState
    data class Loaded(val sections: List<HomeFeedSectionUiState>) : HomeContentUiState
    data class Error(val message: String) : HomeContentUiState
}
