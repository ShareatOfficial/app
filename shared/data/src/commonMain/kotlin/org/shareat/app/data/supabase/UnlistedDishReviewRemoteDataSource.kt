package org.shareat.app.data.supabase

import org.shareat.app.data.supabase.model.SaveUnlistedDishReviewRpc
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageUpload

/**
 * [save] returns only after the creation RPC has confirmed its id. Only
 * [UnlistedDishReviewCreationRejected] proves the transaction did not commit.
 * All other failures, including cancellation and transport errors, are ambiguous.
 */
internal interface UnlistedDishReviewRemoteDataSource {
    suspend fun upload(path: String, image: ImageUpload)
    suspend fun save(parameters: SaveUnlistedDishReviewRpc): String
    suspend fun getById(id: String): UnlistedDishReviewDto?
    suspend fun getByAuthor(accountId: AccountId): List<UnlistedDishReviewDto>
    suspend fun signedImageUrl(path: String): String
    suspend fun deleteImage(path: String)
}

internal class UnlistedDishReviewCreationRejected(val reason: Throwable) : Exception(reason)
