package org.shareat.feature.home.domain

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

private const val MaxDishHighlightsPerRestaurant = 3

class GetRestaurantsUseCaseImpl(
    private val restaurantRepository: RestaurantRepository,
    private val dishRepository: DishRepository,
    private val reviewRepository: ReviewRepository,
) : GetRestaurantsUseCase {
    override suspend fun invoke(
        page: Int,
        numberOfRestaurants: Int,
    ): RepositoryResult<List<RestaurantWithHighlights>> {
//        return FakeHomeRestaurantCatalog.result // Mock implementation Delete whenever supabase is ready
        // repository might implement pagination
        val restaurants = when (val result = restaurantRepository.getPublishedRestaurants()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        return RepositoryResult.Success(
            restaurants.map { restaurant ->
                RestaurantWithHighlights(
                    restaurant = restaurant,
                    ratingSummary = restaurant.ratingSummary(),
                    dishHighlights = restaurant.dishHighlights(),
                    isOpen = true,
                )
            },
        )
    }

    private suspend fun Restaurant.ratingSummary(): RatingSummary =
        when (val result = reviewRepository.getRatingSummary(ReviewTarget.Restaurant(id))) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> RatingSummary(averageTenths = null, ratingCount = 0)
        }

    private suspend fun Restaurant.dishHighlights(): List<DishReviewHighlight> {
        val dishes = when (val result = dishRepository.getDishes(id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyList()
        }
        return dishes
            .flatMap { dish -> dish.reviewsWithComment() }
            .sortedByDescending { (review, _) -> review.createdAt.value }
            .take(MaxDishHighlightsPerRestaurant)
            .map { (review, dish) -> DishReviewHighlight(dish, review) }
    }

    private suspend fun Dish.reviewsWithComment(): List<Pair<Review, Dish>> {
        val reviews = when (val result = reviewRepository.getPublicReviews(ReviewTarget.Dish(id))) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyList()
        }
        return reviews.filter { it.comment != null }.map { it to this }
    }
}
