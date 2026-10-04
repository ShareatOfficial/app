package org.shareat.feature.review.domain

import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.review.domain.model.SubmitUnlistedDishReviewParams

fun interface SubmitUnlistedDishReviewUseCase {
    suspend operator fun invoke(params: SubmitUnlistedDishReviewParams): RepositoryResult<UnlistedDishReview>
}
