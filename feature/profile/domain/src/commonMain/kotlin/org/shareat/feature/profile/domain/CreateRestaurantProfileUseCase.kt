package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.RepositoryResult

fun interface CreateRestaurantProfileUseCase {
    suspend operator fun invoke(
        restaurantProfile: RestaurantProfileDraft,
    ): RepositoryResult<Restaurant>
}
