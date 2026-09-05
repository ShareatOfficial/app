package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeReviewRepositoryTest {
    @Test
    fun ratingSummaryUsesOnlyVisiblePublicReviews() = runSuspend {
        val repository = FakeReviewRepository(FakeShareatData.preview())

        val result = repository.getRatingSummary(ReviewTarget.Restaurant(FakeIds.restaurant))

        val summary = assertIs<RepositoryResult.Success<*>>(result).value
        assertEquals(
            org.shareat.app.domain.model.RatingSummary(averageTenths = 45, ratingCount = 2),
            summary,
        )
    }

    @Test
    fun batchedDishReviewsMatchTheSingleTargetLookup() = runSuspend {
        val repository = FakeReviewRepository(FakeShareatData.preview())

        val batched = assertIs<RepositoryResult.Success<*>>(
            repository.getPublicDishReviews(setOf(FakeIds.croquettes)),
        ).value as Map<*, *>
        val single = assertIs<RepositoryResult.Success<*>>(
            repository.getPublicReviews(ReviewTarget.Dish(FakeIds.croquettes)),
        ).value

        assertEquals(single, batched[FakeIds.croquettes])
    }

    @Test
    fun batchedDishReviewsOmitDishesWithoutPublicReviews() = runSuspend {
        val repository = FakeReviewRepository(FakeShareatData.preview())

        val batched = assertIs<RepositoryResult.Success<*>>(
            repository.getPublicDishReviews(setOf(DishId("dish-with-no-reviews"))),
        ).value as Map<*, *>

        assertEquals(emptyMap<Any, Any>(), batched)
    }

    @Test
    fun savingSameAuthorAndTargetUpdatesInsteadOfDuplicating() = runSuspend {
        val repository = FakeReviewRepository(
            data = FakeShareatData.preview(),
            timestampProvider = FakeTimestampProvider { IsoTimestamp("2026-08-13T13:00:00Z") },
        )
        val target = ReviewTarget.Restaurant(FakeIds.restaurant)
        val original = assertIs<RepositoryResult.Success<*>>(
            repository.getReviewsByAuthor(FakeIds.customerAccount),
        ).value as List<*>
        val originalReview = original
            .filterIsInstance<org.shareat.app.domain.model.Review>()
            .first { it.target == target }

        val saved = repository.saveReview(
            ReviewDraft(
                authorAccountId = FakeIds.customerAccount,
                target = target,
                rating = Rating(3),
                comment = "He cambiado de opinión.",
                visibility = ReviewVisibility.Private,
                visitedAt = IsoTimestamp("2026-08-12T20:00:00Z"),
            ),
        )

        val updated = assertIs<RepositoryResult.Success<*>>(saved).value
        val review = assertIs<org.shareat.app.domain.model.Review>(updated)
        val afterSave = assertIs<RepositoryResult.Success<*>>(
            repository.getReviewsByAuthor(FakeIds.customerAccount),
        ).value as List<*>
        assertEquals(originalReview.id, review.id)
        assertEquals(originalReview.createdAt, review.createdAt)
        assertEquals(IsoTimestamp("2026-08-13T13:00:00Z"), review.updatedAt)
        assertEquals(original.size, afterSave.size)
        assertEquals(1, publicReviews(repository, target))
    }

    @Test
    fun restaurantAccountCannotCreateAReview() = runSuspend {
        val repository = FakeReviewRepository(FakeShareatData.preview())

        val result = repository.saveReview(
            ReviewDraft(
                authorAccountId = FakeIds.restaurantAccount,
                target = ReviewTarget.Restaurant(FakeIds.restaurant),
                rating = Rating(5),
                visibility = ReviewVisibility.Public,
            ),
        )

        val failure = assertIs<RepositoryResult.Failure>(result)
        assertIs<RepositoryError.Conflict>(failure.error)
        Unit
    }

    @Test
    fun unknownReviewTargetIsRejected() = runSuspend {
        val repository = FakeReviewRepository(FakeShareatData.preview())

        val result = repository.saveReview(
            ReviewDraft(
                authorAccountId = FakeIds.customerAccount,
                target = ReviewTarget.Restaurant(RestaurantId("restaurant-missing")),
                rating = Rating(5),
                visibility = ReviewVisibility.Public,
            ),
        )

        assertIs<RepositoryError.NotFound>(assertIs<RepositoryResult.Failure>(result).error)
        Unit
    }

    private suspend fun publicReviews(
        repository: FakeReviewRepository,
        target: ReviewTarget,
    ): Int {
        val result = assertIs<RepositoryResult.Success<*>>(repository.getPublicReviews(target)).value
        return (result as List<*>).size
    }
}
