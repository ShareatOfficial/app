package org.shareat.app.domain.usecase

import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class GetRestaurantsUseCaseImpl(
    private val restaurantRepository: RestaurantRepository,
    private val assembler: RestaurantSummariesAssembler,
) : GetRestaurantsUseCase {
    override suspend fun invoke(
        page: Int,
        numberOfRestaurants: Int,
    ): RepositoryResult<List<RestaurantSummary>> {
        val restaurants = when (val result = restaurantRepository.getPublishedRestaurants()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        return RepositoryResult.Success(
            assembler.assemble(restaurants.drop(page).take(numberOfRestaurants)),
        )
    }
}
