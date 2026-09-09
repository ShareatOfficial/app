package org.shareat.feature.review.domain

import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

public class SubmitDishReviewsUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val dishRepository: DishRepository,
    private val reviewRepository: ReviewRepository,
) : SubmitDishReviewsUseCase {
    override suspend fun invoke(params: SubmitDishReviewsParams): RepositoryResult<Unit> {
        if (params.dishRating !in ValidRatingRange ||
            params.restaurantRating !in ValidRatingRange
        ) {
            return RepositoryResult.Failure(
                RepositoryError.Validation("Ratings must be between 1 and 5."),
            )
        }

        val session = when (val result = authRepository.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val account = when (val result = accountRepository.getAccount(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (account.role != AccountRole.Customer || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(RepositoryError.Forbidden)
        }

        val dish = when (val result = dishRepository.getDish(params.dishId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        val dishReview = ReviewDraft(
            authorAccountId = account.id,
            target = ReviewTarget.Dish(dish.id),
            rating = Rating(params.dishRating),
            comment = params.dishComment.normalizedComment(),
            visibility = ReviewVisibility.Public,
        )
        when (val result = reviewRepository.saveReview(dishReview)) {
            is RepositoryResult.Success -> Unit
            is RepositoryResult.Failure -> return result
        }

        val restaurantReview = ReviewDraft(
            authorAccountId = account.id,
            target = ReviewTarget.Restaurant(dish.restaurantId),
            rating = Rating(params.restaurantRating),
            comment = params.restaurantComment.normalizedComment(),
            visibility = ReviewVisibility.Public,
        )
        return when (val result = reviewRepository.saveReview(restaurantReview)) {
            is RepositoryResult.Success -> RepositoryResult.Success(Unit)
            is RepositoryResult.Failure -> result
        }
    }
}

private val ValidRatingRange: IntRange = 1..5

private fun String?.normalizedComment(): String? = this?.trim()?.ifBlank { null }
