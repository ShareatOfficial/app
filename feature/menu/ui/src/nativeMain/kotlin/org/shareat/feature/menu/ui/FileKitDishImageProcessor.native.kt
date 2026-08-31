package org.shareat.feature.menu.ui

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage
import kotlinx.coroutines.CancellationException

/** Native `actual` counterpart of the shared `expect` processor, using FileKit compression. */
actual class FileKitDishImageProcessor : DishImageProcessor {
    actual constructor()
    override suspend fun invoke(displayName: String, bytes: ByteArray): Result<ProcessedDishImage> = try {
        require(bytes.isNotEmpty()) { "The image is empty." }
        val mime = detectDishImageMimeType(bytes) ?: throw IllegalArgumentException("Select a JPEG, PNG, or WebP image.")
        if (bytes.size <= MAX_DISH_IMAGE_BYTES) Result.success(ProcessedDishImage(displayName, bytes, mime)) else {
            val compressed = attempts.firstNotNullOfOrNull { attempt -> FileKit.compressImage(
                bytes = bytes,
                quality = attempt.quality,
                maxWidth = attempt.dimension,
                maxHeight = attempt.dimension,
                imageFormat = ImageFormat.JPEG,
            ).takeIf { it.size <= MAX_DISH_IMAGE_BYTES } }
                ?: throw IllegalArgumentException("The image could not be reduced below 500 KB.")
            Result.success(ProcessedDishImage(displayName, compressed, "image/jpeg"))
        }
    } catch (error: CancellationException) { throw error } catch (error: Throwable) { Result.failure(error) }
}

private data class Attempt(val quality: Int, val dimension: Int)
private val attempts = listOf(Attempt(82, 1600), Attempt(70, 1280), Attempt(58, 1024), Attempt(45, 800))
