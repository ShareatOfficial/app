package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import kotlin.time.Duration.Companion.hours

internal class SupabaseAccountRepository(
    private val client: SupabaseClient,
) : AccountRepository {
    override suspend fun getAccount(id: AccountId): RepositoryResult<Account> = supabaseResult {
        val user = client.auth.currentUserOrNull()
        val dto = client.from("accounts").select {
            filter { eq("id", id.value) }
        }.decodeList<AccountDto>().singleOrNull()
            ?: throw DomainNotFound("account", id.value)
        dto.toDomain(requireNotNull(user?.email))
    }

    override suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile> = supabaseResult {
        val dto = client.from("customer_profiles").select {
            filter { eq("account_id", accountId.value) }
        }.decodeList<CustomerProfileDto>().singleOrNull()
            ?: throw DomainNotFound("customer profile", accountId.value)
        dto.toDomain()
    }

    override suspend fun updateCustomerProfile(
        profile: CustomerProfile,
    ): RepositoryResult<CustomerProfile> = supabaseResult {
        val dto = client.from("customer_profiles").update({
            set("full_name", profile.fullName)
            set("display_name", profile.displayName)
            set("phone_number", profile.phoneNumber)
            set("preferred_language", profile.preferredLanguage)
        }) {
            select()
            filter { eq("account_id", profile.accountId.value) }
        }.decodeList<CustomerProfileDto>().singleOrNull()
            ?: throw DomainForbidden()
        dto.toDomain()
    }

    private suspend fun CustomerProfileDto.toDomain() = CustomerProfile(
        accountId = AccountId(accountId),
        displayName = displayName,
        avatar = avatarPath?.let { path ->
            ImageRef(
                url = client.storage.from("avatars").createSignedUrl(path, 1.hours),
                alternativeText = avatarAltText,
            )
        },
        fullName = fullName,
        phoneNumber = phoneNumber,
        preferredLanguage = preferredLanguage,
    )
}

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

internal class SupabaseDishRepository(
    private val client: SupabaseClient,
) : DishRepository {
    override suspend fun getDish(id: DishId): RepositoryResult<Dish> = supabaseResult {
        val dto = client.from("dishes").select {
            filter { eq("id", id.value) }
        }.decodeList<DishDto>().singleOrNull() ?: throw DomainNotFound("dish", id.value)
        dto.toDomain(loadAllergens(setOf(dto.id))[dto.id].orEmpty(), ::dishImageUrl)
    }

    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> = supabaseResult {
        val dishes = client.from("dishes").select {
            filter { eq("restaurant_id", restaurantId.value) }
        }.decodeList<DishDto>()
        val allergens = loadAllergens(dishes.mapTo(mutableSetOf(), DishDto::id))
        dishes.map { it.toDomain(allergens[it.id].orEmpty(), ::dishImageUrl) }
    }

    private suspend fun loadAllergens(dishIds: Set<String>): Map<String, Set<String>> {
        if (dishIds.isEmpty()) return emptyMap()
        return client.from("dish_allergens").select().decodeList<DishAllergenDto>()
            .asSequence()
            .filter { it.dishId in dishIds }
            .groupBy(DishAllergenDto::dishId, DishAllergenDto::allergenId)
            .mapValues { it.value.toSet() }
    }

    private fun dishImageUrl(path: String): String = client.storage.from("dish-images").publicUrl(path)
}
