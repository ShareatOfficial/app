package org.shareat.feature.profile.ui.onboarding

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage

actual class FileKitRestaurantImageProcessor actual constructor() : RestaurantImageProcessor {
    override suspend fun invoke(
        displayName: String,
        bytes: ByteArray,
    ): Result<ProcessedRestaurantImage> = processAndCompressRestaurantImage(displayName, bytes)
}

private suspend fun processAndCompressRestaurantImage(
    displayName: String,
    bytes: ByteArray,
): Result<ProcessedRestaurantImage> = runCatching {
    require(bytes.isNotEmpty()) { "La imagen está vacía." }
    val originalMime = detectRestaurantImageMimeType(bytes)
        ?: throw IllegalArgumentException("Selecciona una imagen JPEG, PNG o WebP.")
    if (bytes.size <= MAX_RESTAURANT_IMAGE_BYTES) {
        return@runCatching ProcessedRestaurantImage(displayName, bytes, originalMime)
    }
    val compressed = compressionAttempts.firstNotNullOfOrNull { attempt ->
        FileKit.compressImage(
            bytes = bytes,
            quality = attempt.quality,
            maxWidth = attempt.maxDimension,
            maxHeight = attempt.maxDimension,
            imageFormat = ImageFormat.JPEG,
        ).takeIf { it.size <= MAX_RESTAURANT_IMAGE_BYTES }
    } ?: throw IllegalArgumentException("No se pudo reducir la imagen por debajo de 500 KB.")
    ProcessedRestaurantImage(displayName, compressed, "image/jpeg")
}

private val compressionAttempts = listOf(
    CompressionAttempt(82, 1600),
    CompressionAttempt(70, 1280),
    CompressionAttempt(58, 1024),
    CompressionAttempt(45, 800),
)

private data class CompressionAttempt(val quality: Int, val maxDimension: Int)
