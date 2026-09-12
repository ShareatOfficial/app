package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.model.RestaurantHome

fun interface GetRestaurantHomeUseCase {
    suspend operator fun invoke(): RepositoryResult<RestaurantHome>
}
