package org.shareat.feature.profile.ui.onboarding

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage
import kotlinx.coroutines.CancellationException

/** Native [actual] implementation that uses FileKit to compress oversized restaurant images. */
actual class FileKitRestaurantImageProcessor : RestaurantImageProcessor {
    actual constructor()

    override suspend fun invoke(
        displayName: String,
        bytes: ByteArray,
    ): Result<ProcessedRestaurantImage> = try {
        require(bytes.isNotEmpty()) { "La imagen está vacía." }
        val originalMime = detectRestaurantImageMimeType(bytes)
            ?: throw IllegalArgumentException("Selecciona una imagen JPEG, PNG o WebP.")
        if (bytes.size <= MAX_RESTAURANT_IMAGE_BYTES) {
            Result.success(ProcessedRestaurantImage(displayName, bytes, originalMime))
        } else {
            val attempts = listOf(
                CompressionAttempt(82, 1600),
                CompressionAttempt(70, 1280),
                CompressionAttempt(58, 1024),
                CompressionAttempt(45, 800),
            )
            val compressed = attempts.firstNotNullOfOrNull { attempt ->
                FileKit.compressImage(
                    bytes = bytes,
                    quality = attempt.quality,
                    maxWidth = attempt.maxDimension,
                    maxHeight = attempt.maxDimension,
                    imageFormat = ImageFormat.JPEG,
                ).takeIf { it.size <= MAX_RESTAURANT_IMAGE_BYTES }
            } ?: throw IllegalArgumentException("No se pudo reducir la imagen por debajo de 500 KB.")
            Result.success(ProcessedRestaurantImage(displayName, compressed, "image/jpeg"))
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

private data class CompressionAttempt(val quality: Int, val maxDimension: Int)
