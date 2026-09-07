package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult

class GetRestaurantMenuUseCaseImpl(
    private val assembler: PublishedMenuAssembler,
) : GetRestaurantMenuUseCase {
    override suspend fun invoke(id: RestaurantId): RepositoryResult<RestaurantMenu?> = assembler.assemble(id)
}
