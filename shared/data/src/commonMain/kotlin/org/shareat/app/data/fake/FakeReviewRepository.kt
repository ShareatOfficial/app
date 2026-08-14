package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

fun interface FakeTimestampProvider {
    fun now(): IsoTimestamp
}

class FakeReviewRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
    private val timestampProvider: FakeTimestampProvider = FakeTimestampProvider {
        IsoTimestamp("2026-08-13T12:30:00Z")
    },
) : ReviewRepository {
    override suspend fun getPublicReviews(target: ReviewTarget) = scenario.result(
        populated = {
            data.reviews.filter {
                it.target == target &&
                    it.visibility == ReviewVisibility.Public &&
                    it.moderationStatus == ReviewModerationStatus.Visible
            }
        },
        empty = { emptyList() },
    )

    override suspend fun getReviewsByAuthor(accountId: AccountId) = scenario.result(
        populated = { data.reviews.filter { it.authorAccountId == accountId } },
        empty = { emptyList() },
    )

    override suspend fun getRatingSummary(target: ReviewTarget) = scenario.result(
        populated = {
            val ratings = data.reviews.filter {
                it.target == target &&
                    it.visibility == ReviewVisibility.Public &&
                    it.moderationStatus == ReviewModerationStatus.Visible
            }.map { it.rating.value }
            if (ratings.isEmpty()) {
                RatingSummary(averageTenths = null, ratingCount = 0)
            } else {
                RatingSummary(
                    averageTenths = (ratings.sum() * 10 + ratings.size / 2) / ratings.size,
                    ratingCount = ratings.size,
                )
            }
        },
        empty = { RatingSummary(averageTenths = null, ratingCount = 0) },
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
                moderationStatus = ReviewModerationStatus.Visible,
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
        FakeDataScenario.Unavailable -> RepositoryResult.Failure(RepositoryError.Unavailable)
    }
}
