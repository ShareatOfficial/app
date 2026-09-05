package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary

data class DishCardUiState(
    val id: String,
    val name: String,
    val priceLabel: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val reviews: List<DishReviewUiState> = emptyList(),
    val allergens: List<EuAllergen> = emptyList(),
    val selectedRating: Int? = null,
    val isExpanded: Boolean = false,
) {
    private val ratingSummary: RatingSummary
        get() = RatingSummary.of(reviews.map { Rating(it.rating) })

    val reviewCount: Int get() = ratingSummary.ratingCount

    val ratingLabel: String? get() = ratingSummary.averageTenths?.toRatingLabel()

    val comments: List<DishReviewUiState> get() = reviews.filter { it.comment != null }
}

data class DishReviewUiState(
    val id: String,
    val rating: Int,
    val comment: String? = null,
)
