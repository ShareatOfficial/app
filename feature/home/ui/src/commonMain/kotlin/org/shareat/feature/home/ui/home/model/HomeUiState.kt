package org.shareat.feature.home.ui.home.model

data class HomeUiState(
    val searchQuery: String = "",
    val content: HomeContentUiState = HomeContentUiState.Loading,
)
