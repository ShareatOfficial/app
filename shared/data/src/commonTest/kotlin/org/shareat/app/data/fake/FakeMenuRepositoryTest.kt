package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.MenuDraft
import org.shareat.app.domain.repository.RepositoryResult

class FakeMenuRepositoryTest {
    @Test
    fun publishedMenuJoinsEnabledDishesInMenuOrder() = runSuspend {
        val repository = FakeMenuRepository(FakeShareatData.preview())

        val result = repository.getPublishedMenu(FakeIds.restaurant)

        val details = assertIs<RepositoryResult.Success<*>>(result).value
        val menuDetails = assertIs<org.shareat.app.domain.model.MenuDetails>(details)
        assertEquals(FakeIds.menu, menuDetails.menu.id)
        assertEquals(listOf(FakeIds.octopus, FakeIds.croquettes), menuDetails.items.map { it.dish.id })
        assertEquals(listOf(1_800L, 1_200L), menuDetails.items.map { it.price.minorUnits })
    }

    @Test
    fun createsExactlyOneDraftMenuForAnEmptyRestaurant() = runSuspend {
        val repository = FakeMenuRepository(FakeShareatData.empty())

        val created = repository.createDraftMenu(
            FakeIds.restaurant,
            MenuDraft(name = "Carta de verano", description = "Para compartir"),
        )
        val repeated = repository.createDraftMenu(FakeIds.restaurant, MenuDraft(name = "Otra carta"))

        assertEquals("Carta de verano", assertIs<RepositoryResult.Success<*>>(created).value.let {
            assertIs<org.shareat.app.domain.model.Menu>(it).name
        })
        assertIs<RepositoryResult.Failure>(repeated)
        Unit
    }
}
