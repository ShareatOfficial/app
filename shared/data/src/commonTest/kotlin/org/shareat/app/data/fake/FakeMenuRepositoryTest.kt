package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.repository.RepositoryResult

class FakeMenuRepositoryTest {
    @Test
    fun publishedMenuJoinsEnabledDishesInMenuOrder() = runSuspend {
        val repository = FakeMenuRepository(FakeShareatData.preview())

        val result = repository.getPublishedMenu(FakeIds.restaurant)

        val details = assertIs<RepositoryResult.Success<*>>(result).value
        val menuDetails = assertIs<MenuDetails>(details)
        assertEquals(FakeIds.carta, menuDetails.menu.id)
        assertEquals(
            listOf(FakeIds.octopus, FakeIds.croquettes, FakeIds.russianSalad),
            menuDetails.items.take(3).map { it.dish.id },
        )
        assertEquals(listOf(1_800L, 1_200L, 1_400L), menuDetails.items.take(3).map { it.price.minorUnits })
    }

    @Test
    fun everyPublishedMenuOfTheRestaurantIsListed() = runSuspend {
        val repository = FakeMenuRepository(FakeShareatData.preview())

        val result = repository.getMenus(FakeIds.restaurant)

        val menus = assertIs<RepositoryResult.Success<List<Menu>>>(result).value
        assertEquals(listOf("Carta", "Menú de temporada"), menus.map(Menu::name))
        assertEquals(
            listOf(FakeIds.seasonalMenu),
            menus.filter { it.publicationState != MenuPublicationState.Published }.map(Menu::id),
        )
    }

    @Test
    fun menuItemsCarryTheirDishCategory() = runSuspend {
        val repository = FakeMenuRepository(FakeShareatData.preview())

        val result = repository.getMenu(FakeIds.carta)

        val menuDetails = assertIs<MenuDetails>(assertIs<RepositoryResult.Success<*>>(result).value)
        assertEquals(8, menuDetails.items.size)
        assertEquals(DishCategory.Starters, menuDetails.items.first().category)
        assertEquals(DishCategory.SmallBites, menuDetails.items.last().category)
    }
}
