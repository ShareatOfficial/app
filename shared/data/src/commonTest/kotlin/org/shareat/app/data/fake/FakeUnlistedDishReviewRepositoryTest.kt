package org.shareat.app.data.fake

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class FakeUnlistedDishReviewRepositoryTest {
    @Test fun repeatedVisitsAreSeparateReviewsAndRemainVisibleToTheirAuthor() = runTest {
        val data = FakeShareatData.preview()
        var now = IsoTimestamp("2026-01-01T12:00:00Z")
        val writer = FakeUnlistedDishReviewRepository(data, timestampProvider = FakeTimestampProvider { now })
        val reader = FakeUnlistedDishReviewRepository(data)
        val first = assertIs<RepositoryResult.Success<UnlistedDishReview>>(writer.create(draft)).value
        now = IsoTimestamp("2026-02-01T12:00:00Z")
        val second = assertIs<RepositoryResult.Success<UnlistedDishReview>>(writer.create(draft)).value
        assertNotEquals(first.id, second.id)
        assertEquals(ReviewModerationStatus.Hidden, first.moderationStatus)
        assertEquals(ReviewVisibility.Private, first.visibility)
        assertEquals(listOf(second, first), assertIs<RepositoryResult.Success<List<UnlistedDishReview>>>(reader.getByAuthor(FakeIds.customerAccount)).value)
        assertEquals(emptyList(), assertIs<RepositoryResult.Success<List<UnlistedDishReview>>>(reader.getByAuthor(FakeIds.secondCustomerAccount)).value)
    }

    @Test fun restaurantAccountsCannotCreateAndFailuresAreTyped() = runTest {
        val data = FakeShareatData.preview()
        assertEquals(RepositoryResult.Failure(RepositoryError.Forbidden), FakeUnlistedDishReviewRepository(data).create(draft.copy(authorAccountId = FakeIds.restaurantAccount)))
        val offline = FakeUnlistedDishReviewRepository(data, FakeDataScenario.Offline)
        assertEquals(RepositoryResult.Failure(RepositoryError.Offline), offline.create(draft))
        assertEquals(RepositoryResult.Failure(RepositoryError.Offline), offline.getByAuthor(FakeIds.customerAccount))
        assertEquals(emptyList(), assertIs<RepositoryResult.Success<List<UnlistedDishReview>>>(FakeUnlistedDishReviewRepository(data, FakeDataScenario.Empty).getByAuthor(FakeIds.customerAccount)).value)
    }
}

private val draft = UnlistedDishReviewDraft(
    authorAccountId = FakeIds.customerAccount,
    restaurantName = "Bar del barrio",
    dishName = "Tortilla",
    image = ImageUpload(byteArrayOf(1), "image/jpeg"),
    rating = Rating(4),
    comment = "Muy buena",
    visibility = ReviewVisibility.Private,
)
