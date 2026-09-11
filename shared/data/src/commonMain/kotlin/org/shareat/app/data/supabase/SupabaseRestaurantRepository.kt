package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.toCreateProfileRpc
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toUpdateSettingsRpc
import org.shareat.app.data.supabase.model.OpeningPeriodDto
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

internal class SupabaseRestaurantRepository(
    private val client: SupabaseClient,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>> = supabaseResult {
        val restaurants = client.from("restaurants").select {
            filter { eq("publication_state", "published") }
        }.decodeList<RestaurantDto>()
        val periods = client.from("restaurant_opening_periods").select().decodeList<OpeningPeriodDto>()
            .groupBy(OpeningPeriodDto::restaurantId)
        restaurants.map { it.toDomain(periods[it.id].orEmpty(), ::restaurantImageUrl) }
    }

    override suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant> = supabaseResult {
        findRestaurant("id", id.value) ?: throw DomainNotFound("restaurant", id.value)
    }

    override suspend fun getRestaurantForOwner(accountId: AccountId): RepositoryResult<Restaurant> = supabaseResult {
        findRestaurant("owner_account_id", accountId.value)
            ?: throw DomainNotFound("restaurant for owner", accountId.value)
    }

    override suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ): RepositoryResult<Restaurant> = supabaseResult {
        val restaurantId = client.postgrest.rpc(
            function = "create_restaurant_profile",
            parameters = draft.toCreateProfileRpc(),
        ).decodeAs<String>()
        val restaurant = findRestaurant("id", restaurantId)
            ?: throw DomainNotFound("restaurant", restaurantId)
        if (restaurant.ownerAccountId != ownerAccountId) throw DomainForbidden()
        restaurant
    }

    override suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant> = supabaseResult {
        client.postgrest.rpc(
            function = "update_restaurant_settings",
            parameters = restaurant.toUpdateSettingsRpc(),
        )
        findRestaurant("id", restaurant.id.value)
            ?: throw DomainNotFound("restaurant", restaurant.id.value)
    }

    private suspend fun findRestaurant(column: String, value: String): Restaurant? {
        val dto = client.from("restaurants").select {
            filter { eq(column, value) }
        }.decodeList<RestaurantDto>().singleOrNull() ?: return null
        val periods = client.from("restaurant_opening_periods").select {
            filter { eq("restaurant_id", dto.id) }
        }.decodeList<OpeningPeriodDto>()
        return dto.toDomain(periods, ::restaurantImageUrl)
    }

    private fun restaurantImageUrl(path: String): String =
        client.storage.from("restaurant-images").publicUrl(path)
}
