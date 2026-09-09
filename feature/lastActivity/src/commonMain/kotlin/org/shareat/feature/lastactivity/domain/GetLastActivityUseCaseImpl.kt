package org.shareat.feature.lastactivity.domain

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

class GetLastActivityUseCaseImpl(
    private val reviewRepository: ReviewRepository,
    private val dishRepository: DishRepository,
    private val restaurantRepository: RestaurantRepository,
) : GetLastActivityUseCase {
    override suspend fun invoke(accountId: AccountId): RepositoryResult<List<LastActivityItem>> {
        val reviews = when (val result = reviewRepository.getReviewsByAuthor(accountId)) {
            is RepositoryResult.Success -> result.value.sortedByDescending { it.updatedAt.value }
            is RepositoryResult.Failure -> return result
        }

        val items = buildList {
            for (review in reviews) {
                val target = when (val reviewTarget = review.target) {
                    is ReviewTarget.Dish -> when (val result = dishRepository.getDish(reviewTarget.dishId)) {
                        is RepositoryResult.Success -> ReviewedTarget.Dish(result.value)
                        is RepositoryResult.Failure -> return result
                    }
                    is ReviewTarget.Restaurant -> when (val result = restaurantRepository.getRestaurant(reviewTarget.restaurantId)) {
                        is RepositoryResult.Success -> ReviewedTarget.Restaurant(result.value)
                        is RepositoryResult.Failure -> return result
                    }
                }
                add(LastActivityItem(review, target))
            }
        }
        return RepositoryResult.Success(items)
    }
}
