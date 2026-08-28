package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.RepositoryResult

fun interface UpdateRestaurantInfoUseCase {
    suspend operator fun invoke(
        params: UpdateRestaurantInfoParams,
    ): RepositoryResult<Restaurant>
}