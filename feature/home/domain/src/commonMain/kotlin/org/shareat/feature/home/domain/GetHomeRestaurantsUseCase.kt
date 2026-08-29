package org.shareat.feature.home.domain

import org.shareat.app.domain.repository.RepositoryResult

fun interface GetHomeRestaurantsUseCase {
    suspend operator fun invoke(offset: Int, limit: Int): RepositoryResult<List<RestaurantWithHighlights>>
}
