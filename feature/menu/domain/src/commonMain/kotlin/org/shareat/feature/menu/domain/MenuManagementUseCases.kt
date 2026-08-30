package org.shareat.feature.menu.domain

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

data class MenuManagementData(
    val restaurant: Restaurant,
    val menu: MenuDetails?,
    val dishes: List<Dish>,
)

fun interface LoadMenuManagementUseCase { suspend operator fun invoke(): RepositoryResult<MenuManagementData> }
fun interface SaveRestaurantMenuUseCase { suspend operator fun invoke(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails> }
fun interface SaveDishUseCase { suspend operator fun invoke(draft: DishDraft): RepositoryResult<Dish> }
fun interface ArchiveDishUseCase { suspend operator fun invoke(id: DishId): RepositoryResult<Unit> }
fun interface DeleteDishUseCase { suspend operator fun invoke(id: DishId): RepositoryResult<Unit> }
fun interface DeleteMenuUseCase { suspend operator fun invoke(id: MenuId): RepositoryResult<Unit> }

class LoadMenuManagementUseCaseImpl(
    private val auth: AuthRepository,
    private val restaurants: RestaurantRepository,
    private val menus: MenuRepository,
    private val dishes: DishRepository,
) : LoadMenuManagementUseCase {
    override suspend fun invoke(): RepositoryResult<MenuManagementData> {
        val session = when (val result = auth.currentSession()) {
            is RepositoryResult.Success -> result.value ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurants.getRestaurantForOwner(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val loadedMenus = when (val result = menus.getMenus(restaurant.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val menu = loadedMenus.singleOrNull()?.let { entry ->
            when (val result = menus.getMenu(entry.id)) {
                is RepositoryResult.Success -> result.value
                is RepositoryResult.Failure -> return result
            }
        }
        return when (val result = dishes.getDishes(restaurant.id)) {
            is RepositoryResult.Success -> RepositoryResult.Success(MenuManagementData(restaurant, menu, result.value))
            is RepositoryResult.Failure -> result
        }
    }
}

class SaveRestaurantMenuUseCaseImpl(private val menus: MenuRepository) : SaveRestaurantMenuUseCase {
    override suspend fun invoke(draft: RestaurantMenuDraft) = menus.saveMenu(draft)
}
class SaveDishUseCaseImpl(private val dishes: DishRepository) : SaveDishUseCase {
    override suspend fun invoke(draft: DishDraft) = dishes.saveDish(draft)
}
class ArchiveDishUseCaseImpl(private val dishes: DishRepository) : ArchiveDishUseCase {
    override suspend fun invoke(id: DishId) = dishes.archiveDish(id)
}
class DeleteDishUseCaseImpl(private val dishes: DishRepository) : DeleteDishUseCase {
    override suspend fun invoke(id: DishId) = dishes.deleteDish(id)
}
class DeleteMenuUseCaseImpl(private val menus: MenuRepository) : DeleteMenuUseCase {
    override suspend fun invoke(id: MenuId) = menus.deleteMenu(id)
}
