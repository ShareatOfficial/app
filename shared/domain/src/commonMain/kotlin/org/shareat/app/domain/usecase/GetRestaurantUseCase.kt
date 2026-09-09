package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult

fun interface GetRestaurantUseCase {
    suspend operator fun invoke(id: RestaurantId): RepositoryResult<RestaurantDetails>
}
