package org.shareat.feature.restaurant.domain

import org.shareat.feature.restaurant.domain.model.DishFilterSubject
import org.shareat.feature.restaurant.domain.model.DishFilters

fun interface DishMatchesFiltersUseCase {
    operator fun invoke(dish: DishFilterSubject, filters: DishFilters): Boolean
}
