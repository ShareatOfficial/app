package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.DishAllergenDto
import org.shareat.app.data.supabase.model.DishDto
import org.shareat.app.data.supabase.model.RestaurantDishDto
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult

internal class SupabaseDishRepository(
    private val client: SupabaseClient,
) : DishRepository {
    override suspend fun getDish(id: DishId): RepositoryResult<Dish> = supabaseResult {
        loadDishes(setOf(id.value)).singleOrNull() ?: throw DomainNotFound("dish", id.value)
    }

    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> = supabaseResult {
        val owned = ownershipOf(restaurantIds = setOf(restaurantId.value))
        loadDishes(owned.keys, owned)
    }

    override suspend fun getDishesByRestaurant(
        restaurantIds: Set<RestaurantId>,
    ): RepositoryResult<Map<RestaurantId, List<Dish>>> = supabaseResult {
        if (restaurantIds.isEmpty()) {
            emptyMap()
        } else {
            val owned = ownershipOf(restaurantIds = restaurantIds.map(RestaurantId::value).toSet())
            loadDishes(owned.keys, owned).groupBy(Dish::restaurantId)
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

    /** A dish carries no restaurant of its own, so the owning restaurant is read alongside it. */
    internal suspend fun loadDishes(
        ids: Set<String>,
        knownOwnership: Map<String, RestaurantId> = emptyMap(),
    ): List<Dish> {
        if (ids.isEmpty()) return emptyList()
        val ownership = knownOwnership.ifEmpty { ownershipOf(dishIds = ids) }
        val dishes = selectInBatches(ids) { batch ->
            client.from("dishes").select {
                filter { isIn("id", batch) }
            }.decodeList<DishDto>()
        }
        val allergens = loadAllergens(ids)
        return dishes.mapNotNull { dish ->
            ownership[dish.id]?.let { restaurantId ->
                dish.toDomain(restaurantId, allergens[dish.id].orEmpty(), ::dishImageUrl)
            }
        }
    }

    private suspend fun ownershipOf(
        dishIds: Set<String> = emptySet(),
        restaurantIds: Set<String> = emptySet(),
    ): Map<String, RestaurantId> {
        val rows = when {
            restaurantIds.isNotEmpty() -> selectInBatches(restaurantIds) { batch ->
                client.from("restaurant_dishes").select {
                    filter { isIn("restaurant_id", batch) }
                }.decodeList<RestaurantDishDto>()
            }

            dishIds.isNotEmpty() -> selectInBatches(dishIds) { batch ->
                client.from("restaurant_dishes").select {
                    filter { isIn("dish_id", batch) }
                }.decodeList<RestaurantDishDto>()
            }

            else -> emptyList()
        }
        return rows.associate { it.dishId to RestaurantId(it.restaurantId) }
    }

    private suspend fun loadAllergens(dishIds: Set<String>): Map<String, Set<String>> {
        if (dishIds.isEmpty()) return emptyMap()
        return selectInBatches(dishIds) { batch ->
            client.from("dish_allergens").select {
                filter { isIn("dish_id", batch) }
            }.decodeList<DishAllergenDto>()
        }
            .groupBy(DishAllergenDto::dishId, DishAllergenDto::allergenId)
            .mapValues { it.value.toSet() }
    }

    private fun dishImageUrl(path: String): String = client.storage.from("dish-images").publicUrl(path)
}
