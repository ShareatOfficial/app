package org.shareat.app.data.fake

import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuItem
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeMenuRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
) : MenuRepository {
    override suspend fun getMenus(restaurantId: RestaurantId) = scenario.result(
        populated = { data.menus.filter { it.restaurantId == restaurantId } },
        empty = { emptyList() },
    )

    override suspend fun getPublishedMenu(restaurantId: RestaurantId): RepositoryResult<MenuDetails> = scenario.result(
        populated = {
            val menu = data.menus.firstOrNull {
                it.restaurantId == restaurantId && it.publicationState == MenuPublicationState.Published
            } ?: return RepositoryResult.Failure(RepositoryError.NotFound("PublishedMenu", restaurantId.value))
            details(menu.id, sellableOnly = true)
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("PublishedMenu", restaurantId.value))
        },
    )

    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> = scenario.result(
        populated = {
            if (data.menus.none { it.id == id }) {
                return RepositoryResult.Failure(RepositoryError.NotFound("Menu", id.value))
            }
            details(id)
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("Menu", id.value))
        },
    )

    override suspend fun saveMenu(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails> = scenario.result(
        populated = { save(draft) },
        empty = { save(draft) },
    )

    override suspend fun deleteMenu(id: MenuId): RepositoryResult<Unit> = scenario.result(
        populated = { delete(id) },
        empty = { delete(id) },
    )

    private fun save(draft: RestaurantMenuDraft): MenuDetails {
        val existing = data.menus.firstOrNull { it.restaurantId == draft.restaurantId }
        val id = existing?.id ?: MenuId("menu-${draft.restaurantId.value}")
        val menu = Menu(id, draft.restaurantId, draft.name.trim(), draft.description?.trim()?.ifEmpty { null }, draft.publicationState)
        if (existing == null) data.menus += menu else data.menus[data.menus.indexOf(existing)] = menu
        data.menuItems.removeAll { it.menuId == id }
        data.menuItems += draft.items.sortedBy { it.position }.mapIndexed { position, item ->
            MenuItem(id, item.dishId, item.price, position, item.isEnabled)
        }
        return details(id)
    }

    private fun delete(id: MenuId) {
        if (data.menus.removeAll { it.id == id }) {
            data.menuItems.removeAll { it.menuId == id }
        }
    }

    private fun details(menuId: MenuId, sellableOnly: Boolean = false): MenuDetails {
        val menu = data.menus.first { it.id == menuId }
        val items = data.menuItems
            .filter { it.menuId == menuId && (!sellableOnly || it.isEnabled) }
            .sortedBy { it.position }
            .mapNotNull { item ->
                data.dishes.firstOrNull { it.id == item.dishId && (!sellableOnly || it.isEnabled) }?.let { dish ->
                    MenuDish(dish = dish, price = item.price, position = item.position, isEnabled = item.isEnabled)
                }
            }
        return MenuDetails(menu = menu, items = items)
    }
}
