package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

fun interface UpdateRestaurantInfoUseCase {
    suspend operator fun invoke(
        params: UpdateRestaurantInfoParams,
    ): RepositoryResult<Restaurant>
}

class UpdateRestaurantInfoUseCaseImpl(
    private val restaurantRepository: RestaurantRepository,
) : UpdateRestaurantInfoUseCase {
    override suspend fun invoke(
        params: UpdateRestaurantInfoParams,
    ): RepositoryResult<Restaurant> {
        val current = when (val result = restaurantRepository.getRestaurant(params.restaurantId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return restaurantRepository.updateRestaurant(
            current.copy(
                name = params.name,
                description = params.description,
                publicEmail = params.publicEmail,
                publicPhone = params.publicPhone,
                address = params.address,
                openingHours = params.openingHours,
                publicationState = params.publicationState,
            ),
        )
    }
}
