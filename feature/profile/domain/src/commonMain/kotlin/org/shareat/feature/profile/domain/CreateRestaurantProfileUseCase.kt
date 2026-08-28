package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryResult

data class CreateRestaurantProfileParams(
    val name: String,
    val description: String?,
    val publicEmail: EmailAddress?,
    val publicPhone: String?,
    val address: PostalAddress,
    val openingHours: WeeklyOpeningHours,
)

fun interface CreateRestaurantProfileUseCase {
    suspend operator fun invoke(
        params: CreateRestaurantProfileParams,
    ): RepositoryResult<Restaurant>
}
