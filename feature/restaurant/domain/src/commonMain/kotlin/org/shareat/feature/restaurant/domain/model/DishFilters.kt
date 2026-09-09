package org.shareat.feature.restaurant.domain.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen

data class DishFilterSubject(
    val category: DishCategory?,
    val declaredAllergens: Set<EuAllergen>,
    val declaresAllergens: Boolean,
)

data class DishFilters(
    val category: DishCategory? = null,
    val excludedAllergens: Set<EuAllergen> = emptySet(),
)
