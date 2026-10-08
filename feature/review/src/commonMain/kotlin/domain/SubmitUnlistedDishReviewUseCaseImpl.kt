package org.shareat.feature.review.domain

import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.UnlistedDishReviewRepository
import org.shareat.feature.review.domain.model.SubmitUnlistedDishReviewParams

class SubmitUnlistedDishReviewUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val reviewRepository: UnlistedDishReviewRepository,
) : SubmitUnlistedDishReviewUseCase {
    override suspend fun invoke(params: SubmitUnlistedDishReviewParams): RepositoryResult<UnlistedDishReview> = when {
        params.rating !in 1..5 -> invalid("Rating must be between 1 and 5.")
        params.restaurantName.isBlank() || params.restaurantName.trim().length > 120 -> invalid("Restaurant name must contain 1 to 120 characters.")
        params.dishName.isBlank() || params.dishName.trim().length > 120 -> invalid("Dish name must contain 1 to 120 characters.")
        params.comment.isBlank() || params.comment.trim().length > 2000 -> invalid("Comment must contain 1 to 2000 characters.")
        params.image.mimeType != "image/jpeg" -> invalid("The review image must be JPEG.")
        else -> submit(params)
    }

    private suspend fun submit(params: SubmitUnlistedDishReviewParams): RepositoryResult<UnlistedDishReview> {
        val session = when (val result = authRepository.currentSession()) {
            is RepositoryResult.Success -> result.value ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val account = when (val result = accountRepository.getAccount(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (account.role != AccountRole.Customer || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(RepositoryError.Forbidden)
        }
        return reviewRepository.create(UnlistedDishReviewDraft(
            authorAccountId = account.id,
            restaurantName = params.restaurantName.trim(),
            dishName = params.dishName.trim(),
            image = params.image,
            rating = Rating(params.rating),
            comment = params.comment.trim(),
            visibility = params.visibility,
            visitedAt = params.visitedAt,
        ))
    }
}

private fun invalid(message: String) = RepositoryResult.Failure(RepositoryError.Validation(message))
