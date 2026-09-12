package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class UpdateRestaurantPublicationStateUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
) : UpdateRestaurantPublicationStateUseCase {
    override suspend fun invoke(state: RestaurantPublicationState): RepositoryResult<Restaurant> {
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurantRepository.getRestaurantForOwner(owner.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (restaurant.ownerAccountId != owner.id) return forbidden()
        if (state == RestaurantPublicationState.Published && restaurant.address == null) {
            return RepositoryResult.Failure(
                RepositoryError.Validation(
                    "Add a complete address before publishing the restaurant",
                ),
            )
        }
        // The RPC owns the restaurant/menu invariant and changes both rows in one transaction.
        // Do not duplicate that mutation here: a client-side saga can only approximate atomicity.
        return restaurantRepository.updateRestaurant(restaurant.copy(publicationState = state))
    }
}
