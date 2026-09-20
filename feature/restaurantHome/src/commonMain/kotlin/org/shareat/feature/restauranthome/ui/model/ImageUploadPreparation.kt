package org.shareat.feature.restauranthome.ui.model

import kotlinx.coroutines.CancellationException

internal data class ImageCompression(val maxDimension: Int, val quality: Int)

internal const val CompressedImageMimeType = "image/jpeg"

internal val ImageCompressionSteps = listOf(
    ImageCompression(maxDimension = 1600, quality = 80),
    ImageCompression(maxDimension = 1280, quality = 70),
    ImageCompression(maxDimension = 1024, quality = 60),
)

internal suspend fun preparedImageUpload(
    fileName: String,
    compress: suspend (ImageCompression) -> ByteArray,
): ImageUploadValidationResult {
    var result: ImageUploadValidationResult = ImageUploadValidationResult.InvalidFile
    for (step in ImageCompressionSteps) {
        val bytes = try {
            compress(step)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            return ImageUploadValidationResult.InvalidFile
        }
        result = imageUploadFrom(bytes, CompressedImageMimeType, fileName)
        if (result != ImageUploadValidationResult.TooLarge) return result
    }
    return result
}
