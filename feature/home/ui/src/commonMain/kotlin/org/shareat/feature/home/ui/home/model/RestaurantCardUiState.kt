package org.shareat.feature.home.ui.home.model

import org.shareat.app.domain.model.RestaurantId

data class RestaurantCardUiState(
    val id: RestaurantId,
    val name: String,
    val heroImageUrl: String?,
    val heroImageDescription: String?,
    val ratingLabel: String,
    val isOpen: Boolean,
    val address: String,
    val dishReviews: List<DishReviewUiState>,
)
