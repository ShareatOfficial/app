package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.repository.RepositoryResult

fun interface UpdateRestaurantPublicationStateUseCase {
    suspend operator fun invoke(state: RestaurantPublicationState): RepositoryResult<Restaurant>
}
