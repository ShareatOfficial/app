package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

class RestaurantSummariesAssembler(
    private val dishRepository: DishRepository,
    private val reviewRepository: ReviewRepository,
) {
    suspend fun assemble(restaurants: List<Restaurant>): List<RestaurantSummary> {
        if (restaurants.isEmpty()) return emptyList()

        val restaurantIds = restaurants.mapTo(mutableSetOf(), Restaurant::id)
        val ratingSummaries = reviewRepository.getRestaurantRatingSummaries(restaurantIds).valueOr(emptyMap())
        val dishesByRestaurant = dishRepository.getDishesByRestaurant(restaurantIds).valueOr(emptyMap())
        val dishIds = dishesByRestaurant.values.flatMapTo(mutableSetOf()) { dishes -> dishes.map(Dish::id) }
        val reviewsByDish = reviewRepository.getPublicDishReviews(dishIds).valueOr(emptyMap())

        return restaurants.map { restaurant ->
            RestaurantSummary(
                restaurant = restaurant,
                ratingSummary = ratingSummaries[restaurant.id] ?: RatingSummary.Unrated,
                dishHighlights = dishesByRestaurant[restaurant.id].orEmpty().toDishHighlights(reviewsByDish),
                isOpen = true,
            )
        }
    }
}

private fun <T> RepositoryResult<T>.valueOr(fallback: T): T = when (this) {
    is RepositoryResult.Success -> value
    is RepositoryResult.Failure -> fallback
}
