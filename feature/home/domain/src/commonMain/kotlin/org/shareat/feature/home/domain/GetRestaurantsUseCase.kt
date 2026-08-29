package org.shareat.feature.home.domain

import org.shareat.app.domain.repository.RepositoryResult

fun interface GetRestaurantsUseCase {
    suspend operator fun invoke(page: Int, numberOfRestaurants: Int): RepositoryResult<List<RestaurantWithHighlights>>
}
