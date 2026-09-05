package org.shareat.app.domain.usecase

import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class GetRestaurantsUseCaseImpl(
    private val restaurantRepository: RestaurantRepository,
    private val assembler: RestaurantDetailsAssembler,
) : GetRestaurantsUseCase {
    override suspend fun invoke(
        page: Int,
        numberOfRestaurants: Int,
    ): RepositoryResult<List<RestaurantDetails>> {
        val restaurants = when (val result = restaurantRepository.getPublishedRestaurants()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        return RepositoryResult.Success(
            restaurants
                .drop(page)
                .take(numberOfRestaurants)
                .map { assembler.assemble(it) },
        )
    }
}
