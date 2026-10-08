package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import org.shareat.app.data.supabase.model.SaveUnlistedDishReviewRpc
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageUpload
import kotlin.time.Duration.Companion.hours

internal class SupabaseUnlistedDishReviewRemoteDataSource(
    private val client: SupabaseClient,
) : UnlistedDishReviewRemoteDataSource {
    override suspend fun upload(path: String, image: ImageUpload) {
        client.storage.from(ReviewImagesBucket).upload(path, image.bytes) {
            upsert = false
            contentType = ContentType.Image.JPEG
        }
    }

    override suspend fun save(parameters: SaveUnlistedDishReviewRpc): String = try {
        client.postgrest.rpc("save_unlisted_dish_review", parameters).decodeAs<String>()
    } catch (error: PostgrestRestException) {
        // These PostgreSQL errors abort the transaction containing the RPC. HTTP
        // failures, timeouts and undecodable success responses do not prove rollback.
        if (isDefinitiveReviewRejection(error.code)) {
            throw UnlistedDishReviewCreationRejected(error)
        }
        throw error
    }

    override suspend fun getById(id: String): UnlistedDishReviewDto? =
        client.from(ReviewDetailsView).select {
            filter { eq("id", id) }
        }.decodeList<UnlistedDishReviewDto>().singleOrNull()

    override suspend fun getByAuthor(accountId: AccountId): List<UnlistedDishReviewDto> =
        client.from(ReviewDetailsView).select {
            filter { eq("author_account_id", accountId.value) }
        }.decodeList<UnlistedDishReviewDto>()

    override suspend fun signedImageUrl(path: String): String =
        client.storage.from(ReviewImagesBucket).createSignedUrl(path, 1.hours)

    override suspend fun deleteImage(path: String) {
        client.storage.from(ReviewImagesBucket).delete(path)
    }
}

private const val ReviewDetailsView = "unlisted_dish_review_details"
private const val ReviewImagesBucket = "review-images"

internal fun isDefinitiveReviewRejection(code: String?): Boolean =
    code != null && code.length == 5 && (code.startsWith("22") || code.startsWith("23") || code == "42501" || code == "P0001")
