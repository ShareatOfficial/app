package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft

fun interface UpdateOwnerRestaurantInfoUseCase {
    suspend operator fun invoke(draft: OwnerRestaurantInfoDraft): RepositoryResult<Restaurant>
}
