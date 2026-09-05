package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen

data class RestaurantUiState(
    val header: RestaurantHeaderUiState,
    val categories: List<CategoryChipUiState> = emptyList(),
    val allergenFilter: AllergenFilterUiState = AllergenFilterUiState(),
    val dishes: List<DishCardUiState> = emptyList(),
    val hasPublishedMenu: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasVisibleDishes: Boolean get() = dishes.isNotEmpty()
}

data class RestaurantHeaderUiState(
    val name: String,
    val address: String,
    val heroImageUrl: String? = null,
    val heroImageDescription: String? = null,
    val description: String? = null,
    val cuisineLabel: String? = null,
    val priceRangeLabel: String? = null,
    val isVerified: Boolean = false,
    val ratingLabel: String? = null,
    val reviewCount: Int = 0,
)

data class CategoryChipUiState(
    val category: DishCategory?,
    val isSelected: Boolean,
)

data class AllergenFilterUiState(
    val allergens: List<AllergenChipUiState> = emptyList(),
) {
    val hasExclusions: Boolean get() = allergens.any(AllergenChipUiState::isExcluded)
}

data class AllergenChipUiState(
    val allergen: EuAllergen,
    val isExcluded: Boolean,
)
