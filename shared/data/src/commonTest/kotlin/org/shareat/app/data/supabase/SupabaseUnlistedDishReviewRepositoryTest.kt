package org.shareat.app.data.supabase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.shareat.app.data.supabase.model.SaveUnlistedDishReviewRpc
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SupabaseUnlistedDishReviewRepositoryTest {
    @Test fun confirmedCreationRemainsSuccessfulWhenTheDetailsReadFails() = runTest {
        val remote = RecordingRemote().apply { readError = IllegalStateException("read unavailable") }
        val result = assertIs<RepositoryResult.Success<UnlistedDishReview>>(repository(remote).create(draft))

        assertEquals("confirmed-id", result.value.id.value)
        assertEquals(draft.restaurantName, result.value.restaurantName)
        assertEquals(draft.comment, result.value.comment)
        assertEquals(submittedAt, result.value.createdAt)
        assertNull(result.value.image)
        assertEquals(1, remote.saves)
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun confirmedCreationRemainsSuccessfulWhenItsDetailsAreTemporarilyAbsent() = runTest {
        val remote = RecordingRemote().apply { row = null }
        assertIs<RepositoryResult.Success<UnlistedDishReview>>(repository(remote).create(draft))
        assertEquals(1, remote.saves)
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun confirmedCreationRetainsServerMetadataWhenSigningFails() = runTest {
        val remote = RecordingRemote().apply { failingImagePaths += "author/photo.jpg" }
        val result = assertIs<RepositoryResult.Success<UnlistedDishReview>>(repository(remote).create(draft))

        assertEquals(IsoTimestamp("2026-10-04T10:00:00Z"), result.value.updatedAt)
        assertNull(result.value.image)
        assertEquals(1, remote.saves)
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun failedCreationDeletesOnlyItsUploadedImageAndPreservesTheOriginalFailure() = runTest {
        val remote = RecordingRemote().apply {
            saveError = UnlistedDishReviewCreationRejected(IllegalArgumentException("creation rejected"))
            deleteError = IllegalStateException("cleanup unavailable")
        }
        val result = assertIs<RepositoryResult.Failure>(repository(remote).create(draft))

        assertEquals(RepositoryError.Validation("creation rejected"), result.error)
        assertEquals(listOf(remote.uploadedPath), remote.deletedPaths)
        assertEquals(0, remote.reads)
    }

    @Test fun aFailedUploadNeverCallsTheCreationRpc() = runTest {
        val remote = RecordingRemote().apply { uploadError = IllegalStateException("upload unavailable") }
        assertIs<RepositoryResult.Failure>(repository(remote).create(draft))
        assertEquals(0, remote.saves)
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun individualSigningFailurePreservesAllReviewsAndOtherImages() = runTest {
        val remote = RecordingRemote().apply {
            rows = listOf(reviewRow, reviewRow.copy(id = "other", imagePath = "author/other.jpg"))
            failingImagePaths += "author/photo.jpg"
        }
        val result = assertIs<RepositoryResult.Success<List<UnlistedDishReview>>>(repository(remote).getByAuthor(AccountId("author")))

        assertEquals(listOf("confirmed-id", "other"), result.value.map { it.id.value })
        assertNull(result.value[0].image)
        assertEquals("https://example.com/signed/author/other.jpg", result.value[1].image?.url)
        assertEquals(draft.comment, result.value[0].comment)
    }

    @Test fun cancellationDuringPostCommitReadOrSigningPropagatesWithoutDeleting() = runTest {
        listOf(
            RecordingRemote().apply { readError = CancellationException("cancel read") },
            RecordingRemote().apply { signError = CancellationException("cancel signing") },
        ).forEach { remote ->
            assertFailsWith<CancellationException> { repository(remote).create(draft) }
            assertEquals(1, remote.saves)
            assertTrue(remote.deletedPaths.isEmpty())
        }
    }

    @Test fun cancellationDuringCreationPreservesTheImageBecauseTheCommitIsUnknown() = runTest {
        val remote = RecordingRemote().apply { saveError = CancellationException("cancel creation") }
        assertFailsWith<CancellationException> { repository(remote).create(draft) }
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun aTimeoutDuringCreationPreservesTheImageBecauseTheCommitIsUnknown() = runTest {
        val remote = RecordingRemote().apply { saveError = IllegalStateException("request timed out") }
        assertIs<RepositoryResult.Failure>(repository(remote).create(draft))
        assertEquals(1, remote.saves)
        assertTrue(remote.deletedPaths.isEmpty())
    }

    @Test fun onlyKnownPostgresTransactionRejectionsPermitCleanup() {
        listOf("22023", "23503", "23505", "23514", "42501", "P0001").forEach { code ->
            assertTrue(isDefinitiveReviewRejection(code), code)
        }
        listOf(null, "", "503", "08006", "PGRST000", "PGRSTX00", "57014", "XX000").forEach { code ->
            assertEquals(false, isDefinitiveReviewRejection(code), code)
        }
    }

    @Test fun cancellationDuringListSigningIsNotConvertedToAMissingImage() = runTest {
        val remote = RecordingRemote().apply { signError = CancellationException("cancel signing") }
        assertFailsWith<CancellationException> { repository(remote).getByAuthor(AccountId("author")) }
    }
}

private val submittedAt = IsoTimestamp("2026-10-04T09:59:59Z")
private val draft = UnlistedDishReviewDraft(AccountId("author"), "Casa", "Tortilla", ImageUpload(byteArrayOf(1), "image/jpeg"), Rating(4), "Muy buena")
private val reviewRow = UnlistedDishReviewDto(
    "confirmed-id", "author", "Casa", "Tortilla", "author/photo.jpg", 4, "Muy buena", "public", "hidden",
    createdAt = "2026-10-04T10:00:00Z", updatedAt = "2026-10-04T10:00:00Z",
)
private fun repository(remote: RecordingRemote) = SupabaseUnlistedDishReviewRepository(remote) { submittedAt }

private class RecordingRemote : UnlistedDishReviewRemoteDataSource {
    var uploadError: Exception? = null
    var saveError: Exception? = null
    var readError: Exception? = null
    var signError: Exception? = null
    var deleteError: Exception? = null
    var row: UnlistedDishReviewDto? = reviewRow
    var rows = listOf(reviewRow)
    var uploadedPath: String? = null
    var saves = 0
    var reads = 0
    val deletedPaths = mutableListOf<String>()
    val failingImagePaths = mutableSetOf<String>()

    override suspend fun upload(path: String, image: ImageUpload) {
        uploadError?.let { throw it }
        uploadedPath = path
    }
    override suspend fun save(parameters: SaveUnlistedDishReviewRpc): String {
        saves += 1
        saveError?.let { throw it }
        return "confirmed-id"
    }
    override suspend fun getById(id: String): UnlistedDishReviewDto? {
        reads += 1
        readError?.let { throw it }
        return row
    }
    override suspend fun getByAuthor(accountId: AccountId) = rows
    override suspend fun signedImageUrl(path: String): String {
        signError?.let { throw it }
        check(path !in failingImagePaths) { "signing unavailable" }
        return "https://example.com/signed/$path"
    }
    override suspend fun deleteImage(path: String) {
        deletedPaths += path
        deleteError?.let { throw it }
    }
}
