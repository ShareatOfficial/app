package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.feature.restauranthome.domain.model.OwnerDishDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate

class UpdateOwnerDishUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
    private val dishRepository: DishRepository,
    private val menuRepository: MenuRepository,
) : UpdateOwnerDishUseCase {
    override suspend fun invoke(
        dishId: DishId,
        draft: OwnerDishDraft,
    ): RepositoryResult<OwnerDishUpdate> {
        if (draft.name.isBlank()) {
            return RepositoryResult.Failure(RepositoryError.Validation("Dish name cannot be blank"))
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
        val currentDish = when (val result = dishRepository.getDish(dishId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (currentDish.restaurantId != restaurant.id) return forbidden()
        val menu = when (val result = soleMenuFor(menuRepository, restaurant.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val menuItem = menu.items.singleOrNull { it.dish.id == dishId }
            ?: return RepositoryResult.Failure(RepositoryError.NotFound("MenuItem", dishId.value))

        val savedDish = when (val result = dishRepository.saveDish(
            DishDraft(
                restaurantId = restaurant.id,
                id = dishId,
                name = draft.name.trim(),
                description = draft.description?.trim()?.ifEmpty { null },
                allergenDeclaration = draft.allergenDeclaration,
                isEnabled = draft.isEnabled,
            ),
        )) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val savedMenu = menuRepository.saveMenu(
            RestaurantMenuDraft(
                restaurantId = restaurant.id,
                menuId = menu.menu.id,
                name = menu.menu.name,
                description = menu.menu.description,
                publicationState = menu.menu.publicationState,
                items = menu.items.map { item ->
                    MenuItemDraft(
                        dishId = item.dish.id,
                        price = if (item.dish.id == dishId) draft.price else item.price,
                        position = item.position,
                        isEnabled = if (item.dish.id == dishId) draft.isEnabled else item.isEnabled,
                        category = item.category,
                    )
                },
            ),
        )
        return when (savedMenu) {
            is RepositoryResult.Success -> RepositoryResult.Success(OwnerDishUpdate(savedDish, savedMenu.value))
            is RepositoryResult.Failure -> when (
                dishRepository.saveDish(
                    DishDraft(
                        restaurantId = currentDish.restaurantId,
                        id = currentDish.id,
                        name = currentDish.name,
                        description = currentDish.description,
                        allergenDeclaration = currentDish.allergenDeclaration,
                        isEnabled = currentDish.isEnabled,
                    ),
                )
            ) {
                is RepositoryResult.Success -> savedMenu
                is RepositoryResult.Failure -> RepositoryResult.Failure(
                    RepositoryError.Conflict(
                        "Menu update failed and the original dish could not be restored",
                    ),
                )
            }
        }
    }

}
