package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

private const val MaxDishHighlightsPerRestaurant = 3

class RestaurantDetailsAssembler(
    private val menuRepository: MenuRepository,
    private val dishRepository: DishRepository,
    private val reviewRepository: ReviewRepository,
) {
    suspend fun assemble(restaurant: Restaurant): RestaurantDetails = RestaurantDetails(
        restaurant = restaurant,
        ratingSummary = ratingSummaryOf(ReviewTarget.Restaurant(restaurant.id)),
        dishHighlights = restaurant.dishHighlights(),
        menu = restaurant.publishedMenu(),
        isOpen = true,
    )

    private suspend fun Restaurant.publishedMenu(): RestaurantMenu? {
        val details = when (val result = menuRepository.getPublishedMenu(id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return null
        }
        val sellableDishes = details.items
            .filter { it.isEnabled && it.dish.isEnabled }
            .sortedBy { it.position }
        val reviewsByDish = publicReviewsOf(sellableDishes.mapTo(mutableSetOf()) { it.dish.id })
        return RestaurantMenu(
            menu = details.menu,
            dishes = sellableDishes.map { menuDish ->
                RatedMenuDish(
                    menuDish = menuDish,
                    reviews = reviewsByDish[menuDish.dish.id].orEmpty(),
                )
            },
        )
    }

    private suspend fun ratingSummaryOf(target: ReviewTarget): RatingSummary =
        when (val result = reviewRepository.getRatingSummary(target)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> RatingSummary.Unrated
        }

    private suspend fun publicReviewsOf(dishIds: Set<DishId>): Map<DishId, List<Review>> =
        when (val result = reviewRepository.getPublicDishReviews(dishIds)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyMap()
        }

    private suspend fun Restaurant.dishHighlights(): List<DishReviewHighlight> {
        val dishes = when (val result = dishRepository.getDishes(id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyList()
        }
        val reviewsByDish = publicReviewsOf(dishes.mapTo(mutableSetOf(), Dish::id))
        return dishes
            .flatMap { dish ->
                reviewsByDish[dish.id].orEmpty().filter { it.comment != null }.map { it to dish }
            }
            .sortedByDescending { (review, _) -> review.createdAt.value }
            .take(MaxDishHighlightsPerRestaurant)
            .map { (review, dish) -> DishReviewHighlight(dish, review) }
    }
}
