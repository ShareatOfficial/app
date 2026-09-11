package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository
import org.shareat.feature.restauranthome.domain.model.OwnerRatedMenuDish
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantMenu
import org.shareat.feature.restauranthome.domain.model.RestaurantHome

class GetRestaurantHomeUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer, // TODO change this with authorized repository
    private val restaurantRepository: RestaurantRepository,
    private val menuRepository: MenuRepository,
    private val reviewRepository: ReviewRepository,
) : GetRestaurantHomeUseCase {
    override suspend fun invoke(): RepositoryResult<RestaurantHome> {
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        val restaurant = when (val result = restaurantRepository.getRestaurantForOwner(owner.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (restaurant.ownerAccountId != owner.id) return forbidden()

        val ratingSummary = when (val result = reviewRepository.getRatingSummary(
            ReviewTarget.Restaurant(restaurant.id),
        )) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val menus = when (val result = menuRepository.getMenus(restaurant.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (menus.size > 1) {
            return RepositoryResult.Failure(
                RepositoryError.Conflict("A restaurant owner can manage only one menu"),
            )
        }
        val ownerMenu = menus.singleOrNull()?.let { menu ->
            val details = when (val result = menuRepository.getMenu(menu.id)) {
                is RepositoryResult.Success -> result.value
                is RepositoryResult.Failure -> return result
            }
            if (details.menu.restaurantId != restaurant.id) return forbidden()
            val reviews = reviewsFor(details.items.mapTo(mutableSetOf()) { it.dish.id })
            when (reviews) {
                is RepositoryResult.Success -> OwnerRestaurantMenu(
                    menu = details.menu,
                    dishes = details.items.map { menuDish ->
                        OwnerRatedMenuDish(
                            menuDish = menuDish,
                            reviews = reviews.value[menuDish.dish.id].orEmpty(),
                        )
                    },
                )

                is RepositoryResult.Failure -> return reviews
            }
        }
        return RepositoryResult.Success(
            RestaurantHome(restaurant, ratingSummary, ownerMenu),
        )
    }

    // TODO surround the entire invoke in a try catch to return the error. And divide the code in little more compressible parts
//    private suspend fun getAccount(): Account? {
//        return when (val result = authorizer.authorize()) {
//            is RepositoryResult.Success -> result.value
//            is RepositoryResult.Failure -> null
//        }
//    }

    private suspend fun reviewsFor(
        dishIds: Set<DishId>,
    ): RepositoryResult<Map<DishId, List<Review>>> =
        if (dishIds.isEmpty()) RepositoryResult.Success(emptyMap())
        else reviewRepository.getPublicDishReviews(dishIds)
}
