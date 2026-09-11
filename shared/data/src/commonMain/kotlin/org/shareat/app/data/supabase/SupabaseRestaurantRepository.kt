package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.toCreateProfileRpc
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toUpdateSettingsRpc
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

// The schedule travels nested inside its restaurant. Read separately it was a whole-table read:
// every period of every restaurant, published or not, on each home load.
private val RestaurantColumns = Columns.raw(
    "id,owner_account_id,name,description,hero_image_path,hero_image_alt_text,public_email," +
        "public_phone,street_line,locality,postal_code,region,country_code,currency_code," +
        "latitude,longitude,publication_state," +
        "restaurant_opening_periods(weekday,position,opens_at,closes_at)",
)

internal class SupabaseRestaurantRepository(
    private val client: SupabaseClient,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>> = supabaseResult {
        client.from("restaurants").select(RestaurantColumns) {
            filter { eq("publication_state", "published") }
        }.decodeList<RestaurantDto>().map { it.toDomain(::restaurantImageUrl) }
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

    private suspend fun findRestaurant(column: String, value: String): Restaurant? =
        client.from("restaurants").select(RestaurantColumns) {
            filter { eq(column, value) }
        }.decodeList<RestaurantDto>().singleOrNull()?.toDomain(::restaurantImageUrl)

    private fun restaurantImageUrl(path: String): String =
        client.storage.from("restaurant-images").publicUrl(path)
}
