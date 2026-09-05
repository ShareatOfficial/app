package org.shareat.feature.restaurant.domain

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restaurant.domain.model.DishFilterSubject
import org.shareat.feature.restaurant.domain.model.DishFilters
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DishMatchesFiltersUseCaseImplTest {

    private val dishMatchesFilters = DishMatchesFiltersUseCaseImpl()

    private val croquettes = DishFilterSubject(
        category = DishCategory.Starters,
        declaredAllergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk),
        declaresAllergens = true,
    )
    private val undeclaredSalad = DishFilterSubject(
        category = DishCategory.Starters,
        declaredAllergens = emptySet(),
        declaresAllergens = false,
    )

    @Test
    fun withoutFiltersEveryDishMatches() {
        assertTrue(dishMatchesFilters(croquettes, DishFilters()))
        assertTrue(dishMatchesFilters(undeclaredSalad, DishFilters()))
    }

    @Test
    fun onlyDishesOfTheSelectedCategoryMatch() {
        assertTrue(dishMatchesFilters(croquettes, DishFilters(category = DishCategory.Starters)))
        assertFalse(dishMatchesFilters(croquettes, DishFilters(category = DishCategory.Desserts)))
    }

    @Test
    fun aDishDeclaringAnExcludedAllergenDoesNotMatch() {
        val filters = DishFilters(excludedAllergens = setOf(EuAllergen.Milk))

        assertFalse(dishMatchesFilters(croquettes, filters))
    }

    @Test
    fun aDishDeclaringOnlyOtherAllergensStillMatches() {
        val filters = DishFilters(excludedAllergens = setOf(EuAllergen.Fish))

        assertTrue(dishMatchesFilters(croquettes, filters))
    }

    @Test
    fun aDishWithoutAnAllergenDeclarationSurvivesEveryAllergenFilter() {
        val filters = DishFilters(excludedAllergens = EuAllergen.entries.toSet())

        assertTrue(dishMatchesFilters(undeclaredSalad, filters))
    }

    @Test
    fun categoryAndAllergenFiltersApplyTogether() {
        val filters = DishFilters(
            category = DishCategory.Starters,
            excludedAllergens = setOf(EuAllergen.Milk),
        )

        assertFalse(dishMatchesFilters(croquettes, filters))
        assertTrue(dishMatchesFilters(undeclaredSalad, filters))
    }
}
