package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.UnlistedDishReviewRepository

class FakeUnlistedDishReviewRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
    private val timestampProvider: FakeTimestampProvider = FakeTimestampProvider { IsoTimestamp("2026-10-03T12:30:00Z") },
) : UnlistedDishReviewRepository {
    override suspend fun create(draft: UnlistedDishReviewDraft): RepositoryResult<UnlistedDishReview> {
        when (scenario) {
            FakeDataScenario.Offline -> return RepositoryResult.Failure(RepositoryError.Offline)
            FakeDataScenario.Unavailable -> return RepositoryResult.Failure(RepositoryError.Unavailable())
            else -> Unit
        }
        val account = data.accounts.firstOrNull { it.id == draft.authorAccountId }
            ?: return RepositoryResult.Failure(RepositoryError.NotFound("Account", draft.authorAccountId.value))
        if (account.role != AccountRole.Customer || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(RepositoryError.Forbidden)
        }
        val now = timestampProvider.now()
        val id = ReviewId("unlisted-review-${data.unlistedDishReviews.size + 1}")
        val review = UnlistedDishReview(
            id = id,
            authorAccountId = draft.authorAccountId,
            restaurantName = draft.restaurantName,
            dishName = draft.dishName,
            image = ImageRef("https://images.example.com/reviews/${id.value}.jpg", draft.dishName),
            rating = draft.rating,
            comment = draft.comment,
            visibility = draft.visibility,
            moderationStatus = ReviewModerationStatus.Hidden,
            visitedAt = draft.visitedAt,
            createdAt = now,
            updatedAt = now,
        )
        data.unlistedDishReviews.add(review)
        return RepositoryResult.Success(review)
    }

    override suspend fun getByAuthor(accountId: AccountId) = scenario.result(
        populated = { data.unlistedDishReviews.filter { it.authorAccountId == accountId }.sortedByDescending { it.updatedAt.value } },
        empty = { emptyList() },
    )
}
