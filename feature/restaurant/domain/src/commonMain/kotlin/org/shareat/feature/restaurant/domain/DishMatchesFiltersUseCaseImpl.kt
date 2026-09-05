package org.shareat.feature.restaurant.domain

class DishMatchesFiltersUseCaseImpl : DishMatchesFiltersUseCase {
    override fun invoke(dish: DishFilterSubject, filters: DishFilters): Boolean {
        val matchesCategory = filters.category == null || dish.category == filters.category
        val matchesAllergens = !dish.declaresAllergens ||
            dish.declaredAllergens.none { it in filters.excludedAllergens }
        return matchesCategory && matchesAllergens
    }
}
