package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft

class UpdateOwnerRestaurantInfoUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
) : UpdateOwnerRestaurantInfoUseCase {
    override suspend fun invoke(draft: OwnerRestaurantInfoDraft): RepositoryResult<Restaurant> {
        if (draft.name.isBlank()) {
            return RepositoryResult.Failure(RepositoryError.Validation("Restaurant name cannot be blank"))
        }
        if (draft.publicPhone != null && draft.publicPhone.isBlank()) {
            return RepositoryResult.Failure(RepositoryError.Validation("Restaurant phone cannot be blank"))
        }
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurantRepository.getRestaurantForOwner(owner.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (restaurant.ownerAccountId != owner.id) return forbidden()
        return restaurantRepository.updateRestaurant(
            restaurant.copy(
                name = draft.name.trim(),
                description = draft.description?.trim()?.ifEmpty { null },
                address = draft.address,
                publicEmail = draft.publicEmail,
                publicPhone = draft.publicPhone?.trim(),
                openingHours = draft.openingHours ?: restaurant.openingHours,
            ),
        )
    }
}
