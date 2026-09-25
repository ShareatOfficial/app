package org.shareat.app.domain.repository

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.app.domain.model.ReviewTarget

interface ReviewRepository {
    suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>>

    /** Batched [getPublicReviews] for many dishes at once. Dishes without reviews are absent. */
    suspend fun getPublicDishReviews(
        dishIds: Set<DishId>,
    ): RepositoryResult<Map<DishId, List<Review>>>

    suspend fun getReviewsByAuthor(accountId: AccountId): RepositoryResult<List<Review>>
    suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary>

    /** Batched [getRatingSummary] for many restaurants at once. Unrated restaurants are absent. */
    suspend fun getRestaurantRatingSummaries(
        restaurantIds: Set<RestaurantId>,
    ): RepositoryResult<Map<RestaurantId, RatingSummary>>

    /** Creates or updates the unique review identified by author and target. */
    suspend fun saveReview(draft: ReviewDraft): RepositoryResult<Review>
    suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId): RepositoryResult<Unit>
    suspend fun reportReview(id: ReviewId, reason: ReviewReportReason): RepositoryResult<Unit>
    suspend fun blockReviewAuthor(id: ReviewId): RepositoryResult<AccountId>
}
