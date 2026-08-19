package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

internal class SupabaseImageRepository(
    private val client: SupabaseClient,
) : ImageRepository {
    override suspend fun replaceImage(
        target: ImageTarget,
        upload: ImageUpload,
    ): RepositoryResult<ImageRef> = supabaseResult {
        val location = resolve(target)
        val newPath = "${location.folder}/${randomName()}.${upload.mimeType.extension()}"
        val bucket = client.storage.from(location.bucket)

        bucket.upload(newPath, upload.bytes) {
            upsert = false
            contentType = ContentType.parse(upload.mimeType)
        }

        try {
            updateDatabasePath(target, newPath, upload.alternativeText)
        } catch (error: Throwable) {
            deleteBestEffort(location.bucket, newPath)
            throw error
        }

        location.oldPath?.let { deleteBestEffort(location.bucket, it) }
        ImageRef(
            url = if (location.isPrivate) {
                bucket.createSignedUrl(newPath, 1.hours)
            } else {
                bucket.publicUrl(newPath)
            },
            alternativeText = upload.alternativeText,
        )
    }

    override suspend fun deleteImage(target: ImageTarget): RepositoryResult<Unit> = supabaseResult {
        val location = resolve(target)
        updateDatabasePath(target, null, null)
        location.oldPath?.let { deleteBestEffort(location.bucket, it) }
    }

    private suspend fun resolve(target: ImageTarget): ImageLocation = when (target) {
        is ImageTarget.CustomerAvatar -> {
            val row = client.from("customer_profiles").select {
                filter { eq("account_id", target.accountId.value) }
            }.decodeList<CustomerProfileDto>().singleOrNull()
                ?: throw DomainNotFound("customer profile", target.accountId.value)
            ImageLocation("avatars", target.accountId.value, row.avatarPath, isPrivate = true)
        }
        is ImageTarget.RestaurantHero -> {
            val row = client.from("restaurants").select {
                filter { eq("id", target.restaurantId.value) }
            }.decodeList<RestaurantDto>().singleOrNull()
                ?: throw DomainNotFound("restaurant", target.restaurantId.value)
            ImageLocation("restaurant-images", row.id, row.heroImagePath, isPrivate = false)
        }
        is ImageTarget.DishImage -> {
            val row = client.from("dishes").select {
                filter { eq("id", target.dishId.value) }
            }.decodeList<DishDto>().singleOrNull()
                ?: throw DomainNotFound("dish", target.dishId.value)
            ImageLocation("dish-images", row.restaurantId, row.imagePath, isPrivate = false)
        }
    }

    private suspend fun updateDatabasePath(target: ImageTarget, path: String?, altText: String?) {
        val updatedRows = when (target) {
            is ImageTarget.CustomerAvatar -> client.from("customer_profiles").update({
                set("avatar_path", path)
                set("avatar_alt_text", altText)
            }) {
                select()
                filter { eq("account_id", target.accountId.value) }
            }.decodeList<JsonObject>()
            is ImageTarget.RestaurantHero -> client.from("restaurants").update({
                set("hero_image_path", path)
                set("hero_image_alt_text", altText)
            }) {
                select()
                filter { eq("id", target.restaurantId.value) }
            }.decodeList<JsonObject>()
            is ImageTarget.DishImage -> client.from("dishes").update({
                set("image_path", path)
                set("image_alt_text", altText)
            }) {
                select()
                filter { eq("id", target.dishId.value) }
            }.decodeList<JsonObject>()
        }
        if (updatedRows.isEmpty()) throw DomainForbidden()
    }

    private suspend fun deleteBestEffort(bucket: String, path: String) {
        try {
            client.storage.from(bucket).delete(path)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // The database no longer references this path. A later cleanup job may remove the orphan.
        }
    }
}

private data class ImageLocation(
    val bucket: String,
    val folder: String,
    val oldPath: String?,
    val isPrivate: Boolean,
)

private fun String.extension(): String = when (this) {
    "image/jpeg" -> "jpg"
    "image/png" -> "png"
    "image/webp" -> "webp"
    else -> error("Unsupported image MIME type")
}

private fun randomName(): String = Random.Default.nextBytes(16).joinToString("") { byte ->
    byte.toUByte().toString(16).padStart(2, '0')
}
