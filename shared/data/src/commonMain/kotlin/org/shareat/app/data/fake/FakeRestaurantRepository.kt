package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class FakeRestaurantRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants() = scenario.result(
        populated = { data.restaurants.filter { it.publicationState == RestaurantPublicationState.Published } },
        empty = { emptyList() },
    )

    override suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant> = scenario.result(
        populated = {
            data.restaurants.firstOrNull { it.id == id }
                ?: return RepositoryResult.Failure(RepositoryError.NotFound("Restaurant", id.value))
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("Restaurant", id.value))
        },
    )

    override suspend fun getRestaurantForOwner(accountId: AccountId): RepositoryResult<Restaurant> = scenario.result(
        populated = {
            data.restaurants.firstOrNull { it.ownerAccountId == accountId }
                ?: return RepositoryResult.Failure(RepositoryError.NotFound("RestaurantOwner", accountId.value))
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("RestaurantOwner", accountId.value))
        },
    )

    override suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ): RepositoryResult<Restaurant> = scenario.result(
        populated = {
            data.restaurants.firstOrNull { it.ownerAccountId == ownerAccountId }?.let {
                return RepositoryResult.Success(it)
            }
            val restaurant = Restaurant(
                id = RestaurantId("restaurant-${ownerAccountId.value}"),
                ownerAccountId = ownerAccountId,
                name = draft.name,
                description = draft.description,
                publicEmail = draft.publicEmail,
                publicPhone = draft.publicPhone,
                address = draft.address,
                openingHours = draft.openingHours,
                publicationState = RestaurantPublicationState.Draft,
            )
            data.restaurants += restaurant
            restaurant
        },
        empty = {
            val restaurant = Restaurant(
                id = RestaurantId("restaurant-${ownerAccountId.value}"),
                ownerAccountId = ownerAccountId,
                name = draft.name,
                description = draft.description,
                publicEmail = draft.publicEmail,
                publicPhone = draft.publicPhone,
                address = draft.address,
                openingHours = draft.openingHours,
                publicationState = RestaurantPublicationState.Draft,
            )
            data.restaurants += restaurant
            restaurant
        },
    )

    override suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant> = scenario.result(
        populated = {
            val index = data.restaurants.indexOfFirst { it.id == restaurant.id }
            if (index == -1) {
                return RepositoryResult.Failure(
                    RepositoryError.NotFound("Restaurant", restaurant.id.value),
                )
            }
            data.restaurants[index] = restaurant
            restaurant
        },
        empty = {
            return RepositoryResult.Failure(
                RepositoryError.NotFound("Restaurant", restaurant.id.value),
            )
        },
    )
}
