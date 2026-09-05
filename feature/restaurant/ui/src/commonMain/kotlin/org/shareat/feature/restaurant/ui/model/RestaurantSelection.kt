package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen

data class RestaurantSelection(
    val category: DishCategory? = null,
    val excludedAllergens: Set<EuAllergen> = emptySet(),
    val dishRatings: Map<String, Int> = emptyMap(),
)
