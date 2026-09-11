package org.shareat.app.domain.repository

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft

interface RestaurantRepository {
    suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>>
    suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant>
    suspend fun getRestaurantForOwner(accountId: AccountId): RepositoryResult<Restaurant>
    suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ): RepositoryResult<Restaurant>
    suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant>
}
