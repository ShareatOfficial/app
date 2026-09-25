package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

fun interface CreateOwnerRestaurantUseCase {
    suspend operator fun invoke(name: String): RepositoryResult<Restaurant>
}

class CreateOwnerRestaurantUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurants: RestaurantRepository,
) : CreateOwnerRestaurantUseCase {
    override suspend fun invoke(name: String): RepositoryResult<Restaurant> {
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return restaurants.createRestaurantProfile(
            ownerAccountId = owner.id,
            draft = RestaurantProfileDraft(name = name.trim()),
        )
    }
}
