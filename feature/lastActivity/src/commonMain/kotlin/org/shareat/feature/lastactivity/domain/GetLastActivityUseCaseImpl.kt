package org.shareat.feature.lastactivity.domain

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository
import org.shareat.app.domain.repository.UnlistedDishReviewRepository

class GetLastActivityUseCaseImpl(
    private val reviewRepository: ReviewRepository,
    private val dishRepository: DishRepository,
    private val restaurantRepository: RestaurantRepository,
    private val unlistedReviewRepository: UnlistedDishReviewRepository,
) : GetLastActivityUseCase {
    override suspend fun invoke(accountId: AccountId): RepositoryResult<List<LastActivityItem>> {
        val reviews = when (val result = reviewRepository.getReviewsByAuthor(accountId)) {
            is RepositoryResult.Success -> result.value.sortedByDescending { it.updatedAt.value }
            is RepositoryResult.Failure -> return result
        }

        val unlistedReviews = when (val result = unlistedReviewRepository.getByAuthor(accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val items = buildList<LastActivityItem> {
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
                add(LastActivityItem.Catalog(review, target))
            }
            addAll(unlistedReviews.map { LastActivityItem.Unlisted(it) })
        }
        return RepositoryResult.Success(items.sortedByDescending { it.updatedAt.value })
    }
}
