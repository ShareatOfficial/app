package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult

fun interface GetRestaurantMenuUseCase {
    suspend operator fun invoke(id: RestaurantId): RepositoryResult<RestaurantMenu?>
}
