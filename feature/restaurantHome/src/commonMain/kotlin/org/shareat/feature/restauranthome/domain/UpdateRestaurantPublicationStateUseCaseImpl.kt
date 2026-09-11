package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class UpdateRestaurantPublicationStateUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
    private val menuRepository: MenuRepository,
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
        val menu = when (val result = soleMenuFor(menuRepository, restaurant.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (state == RestaurantPublicationState.Published &&
            menu.items.none { it.isEnabled && it.dish.isEnabled }
        ) {
            return RepositoryResult.Failure(
                RepositoryError.Validation("Publish at least one enabled dish before publishing the restaurant"),
            )
        }
        val updatedRestaurant = restaurant.copy(publicationState = state)
        val updatedMenu = menu.toDraft(state.toMenuPublicationState())

        return if (state == RestaurantPublicationState.Published) {
            publish(menu, updatedRestaurant, updatedMenu)
        } else {
            unpublish(menu, restaurant, updatedRestaurant, updatedMenu)
        }
    }

    /** Publish the menu first so a restaurant never becomes public without a public menu. */
    private suspend fun publish(
        originalMenu: MenuDetails,
        updatedRestaurant: Restaurant,
        updatedMenu: RestaurantMenuDraft,
    ): RepositoryResult<Restaurant> {
        when (val result = menuRepository.saveMenu(updatedMenu)) {
            is RepositoryResult.Success -> Unit
            is RepositoryResult.Failure -> return result
        }
        return when (val restaurantResult = restaurantRepository.updateRestaurant(updatedRestaurant)) {
            is RepositoryResult.Success -> restaurantResult
            is RepositoryResult.Failure -> when (
                menuRepository.saveMenu(originalMenu.toDraft(originalMenu.menu.publicationState))
            ) {
                is RepositoryResult.Success -> restaurantResult
                is RepositoryResult.Failure -> publicationRollbackConflict()
            }
        }
    }

    /** Hide the restaurant first so it cannot remain visible while its menu is being unpublished. */
    private suspend fun unpublish(
        originalMenu: MenuDetails,
        originalRestaurant: Restaurant,
        updatedRestaurant: Restaurant,
        updatedMenu: RestaurantMenuDraft,
    ): RepositoryResult<Restaurant> {
        val restaurantResult = when (
            val result = restaurantRepository.updateRestaurant(updatedRestaurant)
        ) {
            is RepositoryResult.Success -> result
            is RepositoryResult.Failure -> return result
        }
        return when (val menuResult = menuRepository.saveMenu(updatedMenu)) {
            is RepositoryResult.Success -> restaurantResult
            is RepositoryResult.Failure -> when (
                restaurantRepository.updateRestaurant(originalRestaurant)
            ) {
                is RepositoryResult.Success -> menuResult
                is RepositoryResult.Failure -> publicationRollbackConflict()
            }
        }
    }
}

private fun RestaurantPublicationState.toMenuPublicationState(): MenuPublicationState = when (this) {
    RestaurantPublicationState.Published -> MenuPublicationState.Published
    RestaurantPublicationState.Draft -> MenuPublicationState.Unpublished
    RestaurantPublicationState.Disabled -> MenuPublicationState.Disabled
}

private fun MenuDetails.toDraft(state: MenuPublicationState): RestaurantMenuDraft =
    RestaurantMenuDraft(
        restaurantId = menu.restaurantId,
        menuId = menu.id,
        name = menu.name,
        description = menu.description,
        publicationState = state,
        price = menu.price,
        items = items.map { item ->
            MenuItemDraft(
                dishId = item.dish.id,
                price = item.price,
                position = item.position,
                isEnabled = item.isEnabled,
            )
        },
    )

private fun publicationRollbackConflict(): RepositoryResult.Failure = RepositoryResult.Failure(
    RepositoryError.Conflict(
        "Restaurant and menu publication could not be kept consistent because rollback failed",
    ),
)
