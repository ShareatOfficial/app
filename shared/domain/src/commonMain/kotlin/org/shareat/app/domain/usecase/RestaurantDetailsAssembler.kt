package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

class RestaurantDetailsAssembler(
    private val publishedMenuAssembler: PublishedMenuAssembler,
    private val dishRepository: DishRepository,
    private val reviewRepository: ReviewRepository,
) {
    suspend fun assemble(restaurant: Restaurant): RestaurantDetails = RestaurantDetails(
        restaurant = restaurant,
        ratingSummary = ratingSummaryOf(ReviewTarget.Restaurant(restaurant.id)),
        dishHighlights = restaurant.dishHighlights(),
        menu = publishedMenuAssembler.assemble(restaurant.id).valueOrNull(),
        isOpen = true,
    )

    private suspend fun ratingSummaryOf(target: ReviewTarget): RatingSummary =
        when (val result = reviewRepository.getRatingSummary(target)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> RatingSummary.Unrated
        }

    private suspend fun Restaurant.dishHighlights(): List<DishReviewHighlight> {
        val dishes = when (val result = dishRepository.getDishes(id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyList()
        }
        val reviewsByDish =
            when (val result = reviewRepository.getPublicDishReviews(dishes.mapTo(mutableSetOf(), Dish::id))) {
                is RepositoryResult.Success -> result.value
                is RepositoryResult.Failure -> emptyMap()
            }
        return dishes.toDishHighlights(reviewsByDish)
    }
}

private fun <T> RepositoryResult<T?>.valueOrNull(): T? = when (this) {
    is RepositoryResult.Success -> value
    is RepositoryResult.Failure -> null
}
