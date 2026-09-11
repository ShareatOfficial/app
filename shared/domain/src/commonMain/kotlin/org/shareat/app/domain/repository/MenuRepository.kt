package org.shareat.app.domain.repository

import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft

interface MenuRepository {
    suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>>

    /** Every published menu of a published restaurant, oldest first, so "the first one" is stable. */
    suspend fun getPublishedMenus(restaurantId: RestaurantId): RepositoryResult<List<MenuDetails>>

    suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails>
    suspend fun saveMenu(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails>
    suspend fun deleteMenu(id: MenuId): RepositoryResult<Unit>
}
