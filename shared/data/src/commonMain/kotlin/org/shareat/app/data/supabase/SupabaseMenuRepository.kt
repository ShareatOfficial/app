package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import org.shareat.app.data.supabase.mapper.toCurrency
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.MenuItemDto
import org.shareat.app.data.supabase.model.RestaurantCurrencyDto
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryResult

internal class SupabaseMenuRepository(
    private val client: SupabaseClient,
    private val dishes: SupabaseDishRepository,
) : MenuRepository {
    override suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>> = supabaseResult {
        val currency = currencyOf(restaurantId.value)
        client.from("menus").select {
            filter { eq("restaurant_id", restaurantId.value) }
            order("created_at", Order.ASCENDING)
        }.decodeList<MenuDto>().map { it.toDomain(currency) }
    }

    override suspend fun getPublishedMenus(
        restaurantId: RestaurantId,
    ): RepositoryResult<List<MenuDetails>> = supabaseResult {
        val menus = client.from("menus").select {
            filter {
                eq("restaurant_id", restaurantId.value)
                eq("publication_state", "published")
            }
            order("created_at", Order.ASCENDING)
        }.decodeList<MenuDto>()
        if (menus.isEmpty()) emptyList() else loadMenuDetails(menus, currencyOf(restaurantId.value))
    }

    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> = supabaseResult {
        val menu = client.from("menus").select {
            filter { eq("id", id.value) }
        }.decodeList<MenuDto>().singleOrNull() ?: throw DomainNotFound("menu", id.value)
        loadMenuDetails(listOf(menu), currencyOf(menu.restaurantId)).single()
    }

    override suspend fun saveMenu(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails> = supabaseResult {
        val id = client.postgrest.rpc(
            function = "save_restaurant_menu",
            parameters = draft.toSaveRpc(),
        ).decodeAs<String>()
        val menu = client.from("menus").select { filter { eq("id", id) } }
            .decodeList<MenuDto>().singleOrNull() ?: throw DomainNotFound("menu", id)
        loadMenuDetails(listOf(menu), currencyOf(menu.restaurantId)).single()
    }

    override suspend fun deleteMenu(id: MenuId): RepositoryResult<Unit> = supabaseResult {
        val rows = client.from("menus").delete { filter { eq("id", id.value) } }
            .decodeList<MenuDto>()
        if (rows.isEmpty()) throw DomainForbidden()
    }

    private suspend fun loadMenuDetails(menus: List<MenuDto>, currency: Currency): List<MenuDetails> {
        val itemsByMenu = selectInBatches(menus.map(MenuDto::id)) { batch ->
            client.from("menu_items").select {
                filter { isIn("menu_id", batch) }
            }.decodeList<MenuItemDto>()
        }.groupBy(MenuItemDto::menuId)
        val dishById = dishes
            .loadDishes(itemsByMenu.values.flatMapTo(mutableSetOf()) { items -> items.map(MenuItemDto::dishId) })
            .associateBy { it.id.value }
        return menus.map { menu ->
            MenuDetails(
                menu = menu.toDomain(currency),
                items = itemsByMenu[menu.id].orEmpty()
                    .sortedBy(MenuItemDto::position)
                    .mapNotNull { item -> dishById[item.dishId]?.let { item.toDomain(it, currency) } },
            )
        }
    }

    private suspend fun currencyOf(restaurantId: String): Currency =
        client.from("restaurants").select(Columns.list("currency_code")) {
            filter { eq("id", restaurantId) }
        }.decodeList<RestaurantCurrencyDto>().singleOrNull()?.currencyCode?.toCurrency() ?: Currency.Euro
}
