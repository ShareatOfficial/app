package org.shareat.feature.review.domain

import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.repository.RepositoryResult

public data class SubmitDishReviewsParams(
    val dishId: DishId,
    val dishRating: Int,
    val dishComment: String?,
    val restaurantRating: Int,
    val restaurantComment: String?,
)

public fun interface SubmitDishReviewsUseCase {
    public suspend operator fun invoke(
        params: SubmitDishReviewsParams,
    ): RepositoryResult<Unit>
}
