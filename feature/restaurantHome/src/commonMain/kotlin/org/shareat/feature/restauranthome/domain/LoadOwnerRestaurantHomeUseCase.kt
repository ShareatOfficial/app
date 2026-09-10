package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantHome

fun interface LoadOwnerRestaurantHomeUseCase {
    suspend operator fun invoke(): RepositoryResult<OwnerRestaurantHome>
}
