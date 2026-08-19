package org.shareat.app.data.fake

import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
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
            details(menu.id)
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

    private fun details(menuId: MenuId): MenuDetails {
        val menu = data.menus.first { it.id == menuId }
        val items = data.menuItems
            .filter { it.menuId == menuId && it.isEnabled }
            .sortedBy { it.position }
            .mapNotNull { item ->
                data.dishes.firstOrNull { it.id == item.dishId && it.isEnabled }?.let { dish ->
                    MenuDish(dish = dish, price = item.price, position = item.position)
                }
            }
        return MenuDetails(menu = menu, items = items)
    }
}
