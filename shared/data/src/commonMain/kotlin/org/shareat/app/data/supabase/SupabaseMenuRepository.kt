package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.currency
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.MenuItemDto
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryResult

private const val MenuFields = "id,restaurant_id,name,description,publication_state,price_minor_units"

/** The currency belongs to the restaurant, so it is read as part of the menu rather than after it. */
private val MenuColumns = Columns.raw("$MenuFields,restaurants(currency_code)")

/** A whole menu — its price, its items, their dishes and allergens — in a single round trip. */
private val MenuDetailColumns = Columns.raw(
    "$MenuFields,restaurants(currency_code)," +
        "menu_items(dish_id,price_minor_units,position,is_enabled," +
        "dishes(id,name,description,image_path,image_alt_text,allergen_note,is_enabled," +
        "dish_allergens(allergen_id)))",
)

internal class SupabaseMenuRepository(
    private val client: SupabaseClient,
) : MenuRepository {
    override suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>> = supabaseResult {
        client.from("menus").select(MenuColumns) {
            filter { eq("restaurant_id", restaurantId.value) }
            order("created_at", Order.ASCENDING)
        }.decodeList<MenuDto>().map { it.toDomain() }
    }

    override suspend fun getPublishedMenus(
        restaurantId: RestaurantId,
    ): RepositoryResult<List<MenuDetails>> = supabaseResult {
        client.from("menus").select(MenuDetailColumns) {
            filter {
                eq("restaurant_id", restaurantId.value)
                eq("publication_state", "published")
            }
            order("created_at", Order.ASCENDING)
        }.decodeList<MenuDto>().map { it.toDetails(::dishImageUrl) }
    }

    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> = supabaseResult {
        loadMenu(id.value)
    }

    override suspend fun saveMenu(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails> = supabaseResult {
        val id = client.postgrest.rpc(
            function = "save_restaurant_menu",
            parameters = draft.toSaveRpc(),
        ).decodeAs<String>()
        loadMenu(id)
    }

    override suspend fun deleteMenu(id: MenuId): RepositoryResult<Unit> = supabaseResult {
        val rows = client.from("menus").delete { filter { eq("id", id.value) } }
            .decodeList<MenuDto>()
        if (rows.isEmpty()) throw DomainForbidden()
    }

    private suspend fun loadMenu(id: String): MenuDetails =
        client.from("menus").select(MenuDetailColumns) {
            filter { eq("id", id) }
        }.decodeList<MenuDto>().singleOrNull()?.toDetails(::dishImageUrl)
            ?: throw DomainNotFound("menu", id)

    private fun dishImageUrl(path: String): String = client.storage.from("dish-images").publicUrl(path)
}

private fun MenuDto.toDetails(publicImageUrl: (String) -> String): MenuDetails {
    val currency = currency()
    return MenuDetails(
        menu = toDomain(currency),
        items = items
            .sortedBy(MenuItemDto::position)
            .mapNotNull { item ->
                item.dish
                    ?.toDomain(RestaurantId(restaurantId), publicImageUrl)
                    ?.let { dish -> item.toDomain(dish, currency) }
            },
    )
}
