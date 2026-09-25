package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.toRatingSummary
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

fun interface FakeTimestampProvider {
    fun now(): IsoTimestamp
}

private fun FakeShareatData.publicReviewsOf(target: ReviewTarget): List<Review> = reviews.filter {
    it.target == target &&
        it.visibility == ReviewVisibility.Public &&
        it.moderationStatus == ReviewModerationStatus.Visible
}

class FakeReviewRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
    private val timestampProvider: FakeTimestampProvider = FakeTimestampProvider {
        IsoTimestamp("2026-08-13T12:30:00Z")
    },
) : ReviewRepository {
    private val blockedAuthors = mutableSetOf<AccountId>()
    private val reports = mutableSetOf<ReviewId>()

    override suspend fun getPublicReviews(target: ReviewTarget) = scenario.result(
        populated = { data.publicReviewsOf(target).filterNot { it.authorAccountId in blockedAuthors } },
        empty = { emptyList() },
    )

    override suspend fun getPublicDishReviews(dishIds: Set<DishId>) = scenario.result(
        populated = {
            dishIds.associateWith {
                data.publicReviewsOf(ReviewTarget.Dish(it)).filterNot { review -> review.authorAccountId in blockedAuthors }
            }
                .filterValues(List<Review>::isNotEmpty)
        },
        empty = { emptyMap() },
    )

    override suspend fun getReviewsByAuthor(accountId: AccountId) = scenario.result(
        populated = { data.reviews.filter { it.authorAccountId == accountId } },
        empty = { emptyList() },
    )

    override suspend fun getRatingSummary(target: ReviewTarget) = scenario.result(
        populated = {
            data.publicReviewsOf(target).filterNot { it.authorAccountId in blockedAuthors }.toRatingSummary()
        },
        empty = { RatingSummary.Unrated },
    )

    override suspend fun getRestaurantRatingSummaries(restaurantIds: Set<RestaurantId>) = scenario.result(
        populated = {
            restaurantIds
                .associateWith { restaurantId ->
                    data.publicReviewsOf(ReviewTarget.Restaurant(restaurantId))
                        .filterNot { it.authorAccountId in blockedAuthors }
                        .toRatingSummary()
                }
                .filterValues { it.ratingCount > 0 }
        },
        empty = { emptyMap() },
    )

    override suspend fun saveReview(draft: ReviewDraft): RepositoryResult<Review> {
        scenario.failureOrNull()?.let { return it }
        val account = data.accounts.firstOrNull { it.id == draft.authorAccountId }
            ?: return RepositoryResult.Failure(
                RepositoryError.NotFound("Account", draft.authorAccountId.value),
            )
        if (account.role != AccountRole.Customer || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(
                RepositoryError.Conflict("Only active customer accounts can create reviews"),
            )
        }
        if (!targetExists(draft.target)) {
            return RepositoryResult.Failure(RepositoryError.NotFound("ReviewTarget", draft.target.idValue()))
        }

        val now = timestampProvider.now()
        val existingIndex = data.reviews.indexOfFirst {
            it.authorAccountId == draft.authorAccountId && it.target == draft.target
        }
        val review = if (existingIndex >= 0) {
            data.reviews[existingIndex].copy(
                rating = draft.rating,
                comment = draft.comment,
                visibility = draft.visibility,
                visitedAt = draft.visitedAt,
                moderationStatus = if (data.reviews[existingIndex].moderationStatus == ReviewModerationStatus.Removed) {
                    ReviewModerationStatus.Removed
                } else if (draft.comment != data.reviews[existingIndex].comment) {
                    if (draft.comment == null) ReviewModerationStatus.Visible else ReviewModerationStatus.Hidden
                } else {
                    data.reviews[existingIndex].moderationStatus
                },
                updatedAt = now,
            ).also { data.reviews[existingIndex] = it }
        } else {
            Review(
                id = ReviewId("review-${draft.authorAccountId.value}-${draft.target.idValue()}"),
                authorAccountId = draft.authorAccountId,
                target = draft.target,
                rating = draft.rating,
                comment = draft.comment,
                visibility = draft.visibility,
                moderationStatus = if (draft.comment == null) {
                    ReviewModerationStatus.Visible
                } else {
                    ReviewModerationStatus.Hidden
                },
                visitedAt = draft.visitedAt,
                createdAt = now,
                updatedAt = now,
            ).also(data.reviews::add)
        }
        return RepositoryResult.Success(review)
    }

    override suspend fun deleteReview(
        id: ReviewId,
        authorAccountId: AccountId,
    ): RepositoryResult<Unit> {
        scenario.failureOrNull()?.let { return it }
        val removed = data.reviews.removeAll { it.id == id && it.authorAccountId == authorAccountId }
        return if (removed) {
            RepositoryResult.Success(Unit)
        } else {
            RepositoryResult.Failure(RepositoryError.NotFound("Review", id.value))
        }
    }

    override suspend fun reportReview(id: ReviewId, reason: ReviewReportReason): RepositoryResult<Unit> {
        scenario.failureOrNull()?.let { return it }
        if (data.reviews.none { it.id == id }) {
            return RepositoryResult.Failure(RepositoryError.NotFound("Review", id.value))
        }
        reports += id
        return RepositoryResult.Success(Unit)
    }

    override suspend fun blockReviewAuthor(id: ReviewId): RepositoryResult<AccountId> {
        scenario.failureOrNull()?.let { return it }
        val author = data.reviews.firstOrNull { it.id == id }?.authorAccountId
            ?: return RepositoryResult.Failure(RepositoryError.NotFound("Review", id.value))
        blockedAuthors += author
        return RepositoryResult.Success(author)
    }

    private fun targetExists(target: ReviewTarget): Boolean = when (target) {
        is ReviewTarget.Restaurant -> data.restaurants.any { it.id == target.restaurantId }
        is ReviewTarget.Dish -> data.dishes.any { it.id == target.dishId }
    }

    private fun ReviewTarget.idValue(): String = when (this) {
        is ReviewTarget.Restaurant -> restaurantId.value
        is ReviewTarget.Dish -> dishId.value
    }

    private fun FakeDataScenario.failureOrNull(): RepositoryResult.Failure? = when (this) {
        FakeDataScenario.Populated, FakeDataScenario.Empty -> null
        FakeDataScenario.Offline -> RepositoryResult.Failure(RepositoryError.Offline)
        FakeDataScenario.Unavailable -> RepositoryResult.Failure(RepositoryError.Unavailable())
    }
}
