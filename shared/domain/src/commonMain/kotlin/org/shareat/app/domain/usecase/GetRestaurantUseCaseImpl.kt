package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class GetRestaurantUseCaseImpl(
    private val restaurantRepository: RestaurantRepository,
    private val assembler: RestaurantDetailsAssembler,
) : GetRestaurantUseCase {
    override suspend fun invoke(id: RestaurantId): RepositoryResult<RestaurantDetails> =
        when (val result = restaurantRepository.getRestaurant(id)) {
            is RepositoryResult.Success -> RepositoryResult.Success(assembler.assemble(result.value))
            is RepositoryResult.Failure -> result
        }
}
