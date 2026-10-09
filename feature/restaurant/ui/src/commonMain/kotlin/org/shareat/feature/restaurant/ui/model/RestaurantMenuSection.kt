package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.DishCategory

data class RestaurantMenuSection(
    val category: DishCategory?,
    val dishes: List<DishCardUiState>,
) {
    val key: String get() = "section-${category?.name ?: "other"}"
}

/** Group only visible dishes, preserving the menu order within each section. */
internal fun List<DishCardUiState>.menuSections(): List<RestaurantMenuSection> =
    groupBy(DishCardUiState::category).map { (category, dishes) ->
        RestaurantMenuSection(category, dishes)
    }

/** Each section contributes its heading followed by its dishes to the single lazy list. */
internal fun List<RestaurantMenuSection>.headerIndices(leadingItems: Int): List<Int> {
    var nextIndex = leadingItems
    return map { section ->
        nextIndex.also { nextIndex += section.dishes.size + 1 }
    }
}
