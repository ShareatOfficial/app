package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toConfirmedReview
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.UnlistedDishReviewRepository
import kotlin.random.Random
import kotlin.time.Clock

internal class SupabaseUnlistedDishReviewRepository(
    private val remote: UnlistedDishReviewRemoteDataSource,
    private val now: () -> IsoTimestamp = { IsoTimestamp(Clock.System.now().toString()) },
) : UnlistedDishReviewRepository {
    constructor(client: SupabaseClient) : this(SupabaseUnlistedDishReviewRemoteDataSource(client))

    override suspend fun create(draft: UnlistedDishReviewDraft): RepositoryResult<UnlistedDishReview> = supabaseResult {
        val submittedAt = now()
        val filename = Random.Default.nextBytes(16).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        val path = "${draft.authorAccountId.value}/$filename.jpg"
        remote.upload(path, draft.image)
        val id = try {
            remote.save(draft.toSaveRpc(path))
        } catch (error: UnlistedDishReviewCreationRejected) {
            // Only a definitive transaction rejection permits compensation. A timeout
            // or cancellation may hide a committed write, so its image must survive.
            withContext(NonCancellable) {
                try {
                    remote.deleteImage(path)
                } catch (_: Throwable) {
                    // Retain the original failure if best-effort cleanup also fails.
                }
            }
            throw error.reason
        }

        val confirmedReview = draft.toConfirmedReview(id, submittedAt)
        try {
            remote.getById(id)?.withReadableImage() ?: confirmedReview
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Hydration cannot turn a confirmed write into a retryable failure. These
            // provisional timestamps are replaced by server values on the next read.
            confirmedReview
        }
    }

    override suspend fun getByAuthor(accountId: AccountId): RepositoryResult<List<UnlistedDishReview>> = supabaseResult {
        remote.getByAuthor(accountId)
            .sortedByDescending { it.updatedAt }
            .map { row -> row.withReadableImage() }
    }

    private suspend fun UnlistedDishReviewDto.withReadableImage(): UnlistedDishReview {
        val imageUrl = try {
            remote.signedImageUrl(imagePath)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
        return toDomain(imageUrl)
    }
}
