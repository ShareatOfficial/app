package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate

class CreateOwnerDishUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
    private val dishRepository: DishRepository,
    private val menuRepository: MenuRepository,
) : CreateOwnerDishUseCase {
    override suspend fun invoke(draft: OwnerDishCreateDraft): RepositoryResult<OwnerDishUpdate> {
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
        val menu = when (val result = soleMenuFor(menuRepository, restaurant.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val dish = when (val result = dishRepository.saveDish(
            DishDraft(
                restaurantId = restaurant.id,
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
                price = menu.menu.price,
                items = menu.items.map { item ->
                    MenuItemDraft(item.dish.id, item.price, item.position, item.isEnabled)
                } + MenuItemDraft(
                    dishId = dish.id,
                    price = draft.price,
                    position = (menu.items.maxOfOrNull { it.position } ?: -1) + 1,
                    isEnabled = draft.isEnabled,
                ),
            ),
        )
        return when (savedMenu) {
            is RepositoryResult.Success -> RepositoryResult.Success(OwnerDishUpdate(dish, savedMenu.value))
            is RepositoryResult.Failure -> when (dishRepository.deleteDish(dish.id)) {
                is RepositoryResult.Success -> savedMenu
                is RepositoryResult.Failure -> RepositoryResult.Failure(
                    RepositoryError.Conflict("Menu update failed and the created dish could not be removed"),
                )
            }
        }
    }
}

internal suspend fun soleMenuFor(
    menuRepository: MenuRepository,
    restaurantId: org.shareat.app.domain.model.RestaurantId,
): RepositoryResult<org.shareat.app.domain.model.MenuDetails> {
    val menus = when (val result = menuRepository.getMenus(restaurantId)) {
        is RepositoryResult.Success -> result.value
        is RepositoryResult.Failure -> return result
    }
    if (menus.size != 1) {
        return RepositoryResult.Failure(
            if (menus.isEmpty()) RepositoryError.NotFound("Menu", restaurantId.value)
            else RepositoryError.Conflict("A restaurant owner can manage only one menu"),
        )
    }
    val details = menuRepository.getMenu(menus.single().id)
    return when (details) {
        is RepositoryResult.Success -> if (details.value.menu.restaurantId == restaurantId) details else forbidden()
        is RepositoryResult.Failure -> details
    }
}
