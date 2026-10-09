package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestaurantMenuSectionTest {
    @Test
    fun groupingKeepsMenuOrderWithinCategoriesAndIncludesUncategorisedDishes() {
        val dishes = listOf(
            dish("starter-1", DishCategory.Starters),
            dish("other", null),
            dish("dessert", DishCategory.Desserts),
            dish("starter-2", DishCategory.Starters),
        )
        val sections = dishes.menuSections()
        assertEquals(listOf(DishCategory.Starters, null, DishCategory.Desserts), sections.map { it.category })
        assertEquals(listOf("starter-1", "starter-2"), sections.first().dishes.map { it.id })
        assertEquals("section-other", sections[1].key)
        assertEquals(listOf(3, 6, 8), sections.headerIndices(leadingItems = 3))
    }

    @Test
    fun removingDishesRebuildsCategoriesAndScrollTargets() {
        val dishes = listOf(dish("starter", DishCategory.Starters), dish("dessert", DishCategory.Desserts))
        val sections = dishes.filterNot { it.id == "starter" }.menuSections()
        assertEquals(listOf(DishCategory.Desserts), sections.map { it.category })
        assertEquals(listOf(3), sections.headerIndices(leadingItems = 3))
        assertTrue(emptyList<DishCardUiState>().menuSections().isEmpty())
    }

    @Test
    fun mappingPreservesTheDifferenceBetweenMissingAndEmptyAllergenDeclarations() {
        val state = RestaurantArgs(
            id = "restaurant", name = "Restaurant", address = "Address", isOpen = true,
            dishes = listOf(
                DishArgs("unknown", "Unknown", "10€"),
                DishArgs("declared-empty", "Declared empty", "10€", declaresAllergens = true),
                DishArgs(
                    "milk", "Milk", "10€", category = DishCategory.Desserts,
                    allergens = listOf(EuAllergen.Milk), declaresAllergens = true,
                    imageDescription = "A dessert",
                ),
            ),
        ).toUiState()
        assertFalse(state.dishes[0].declaresAllergens)
        assertTrue(state.dishes[1].declaresAllergens)
        assertTrue(state.dishes[1].allergens.isEmpty())
        assertEquals(DishCategory.Desserts, state.dishes[2].category)
        assertEquals("A dessert", state.dishes[2].imageDescription)
        assertEquals(listOf(EuAllergen.Milk), state.dishes[2].allergens)
    }

    private fun dish(id: String, category: DishCategory?) =
        DishCardUiState(id = id, name = id, priceLabel = "10€", category = category)
}
