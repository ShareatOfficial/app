package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.RestaurantDishDto
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult

/**
 * A dish is always read through restaurant_dishes: that is what says whose it is, and nesting the
 * dish and its allergens under it turns three round trips into one.
 */
internal val OwnedDishColumns = Columns.raw(
    "restaurant_id,dishes(id,name,description,image_path,image_alt_text,allergen_note," +
        "is_enabled,dish_allergens(allergen_id))",
)

internal class SupabaseDishRepository(
    private val client: SupabaseClient,
) : DishRepository {
    override suspend fun getDish(id: DishId): RepositoryResult<Dish> = supabaseResult {
        loadDishes(setOf(id.value)).singleOrNull() ?: throw DomainNotFound("dish", id.value)
    }

    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> = supabaseResult {
        ownedDishes("restaurant_id", setOf(restaurantId.value))
    }

    override suspend fun getDishesByRestaurant(
        restaurantIds: Set<RestaurantId>,
    ): RepositoryResult<Map<RestaurantId, List<Dish>>> = supabaseResult {
        if (restaurantIds.isEmpty()) {
            emptyMap()
        } else {
            ownedDishes("restaurant_id", restaurantIds.map(RestaurantId::value).toSet())
                .groupBy(Dish::restaurantId)
        }
    }

    override suspend fun saveDish(draft: DishDraft): RepositoryResult<Dish> = supabaseResult {
        val id = client.postgrest.rpc(
            function = "save_restaurant_dish",
            parameters = draft.toSaveRpc(),
        ).decodeAs<String>()
        loadDishes(setOf(id)).singleOrNull() ?: throw DomainNotFound("dish", id)
    }

    override suspend fun archiveDish(id: DishId): RepositoryResult<Unit> = supabaseResult {
        client.postgrest.rpc(function = "archive_restaurant_dish", parameters = mapOf("p_dish_id" to id.value))
    }

    override suspend fun deleteDish(id: DishId): RepositoryResult<Unit> = supabaseResult {
        client.postgrest.rpc(function = "delete_restaurant_dish", parameters = mapOf("p_dish_id" to id.value))
    }

    internal suspend fun loadDishes(ids: Set<String>): List<Dish> =
        if (ids.isEmpty()) emptyList() else ownedDishes("dish_id", ids)

    private suspend fun ownedDishes(column: String, values: Set<String>): List<Dish> =
        selectInBatches(values) { batch ->
            client.from("restaurant_dishes").select(OwnedDishColumns) {
                filter { isIn(column, batch) }
            }.decodeList<RestaurantDishDto>()
        }.mapNotNull { owned ->
            owned.dish?.toDomain(RestaurantId(owned.restaurantId), ::dishImageUrl)
        }

    private fun dishImageUrl(path: String): String = client.storage.from("dish-images").publicUrl(path)
}
