package org.shareat.feature.lastactivity.ui

import org.shareat.app.domain.model.ReviewId

sealed interface LastActivityUiState {
    data object Initializing : LastActivityUiState
    data object Guest : LastActivityUiState
    data object Loading : LastActivityUiState
    data class Content(val items: List<LastActivityReviewUiState>) : LastActivityUiState
    data object Empty : LastActivityUiState
    data class Error(val error: LastActivityError) : LastActivityUiState
}

data class LastActivityReviewUiState(
    val id: ReviewId,
    val type: LastActivityTargetType,
    val imageUrl: String?,
    val imageDescription: String?,
    val name: String,
    val description: String?,
    val rating: Int,
    val comment: String?,
)
