package org.shareat.shared.media

import org.shareat.app.domain.model.ImageUpload

public sealed interface ImageUploadValidationResult {
    public data class Success(public val upload: ImageUpload) : ImageUploadValidationResult
    public data object UnsupportedFormat : ImageUploadValidationResult
    public data object TooLarge : ImageUploadValidationResult
    public data object InvalidFile : ImageUploadValidationResult
}

/** Pure validation so picker implementations can be tested without a platform file dialogue. */
public fun imageUploadFrom(
    bytes: ByteArray,
    mimeType: String?,
    fileName: String,
): ImageUploadValidationResult {
    val normalizedMimeType = mimeType?.lowercase()?.trim() ?: return ImageUploadValidationResult.UnsupportedFormat
    if (normalizedMimeType !in supportedImageMimeTypes) return ImageUploadValidationResult.UnsupportedFormat
    if (bytes.size > maxImageUploadBytes) return ImageUploadValidationResult.TooLarge
    return runCatching {
        ImageUpload(
            bytes = bytes,
            mimeType = normalizedMimeType,
            alternativeText = fileName.trim().takeIf { it.isNotEmpty() },
        )
    }.fold(
        onSuccess = ImageUploadValidationResult::Success,
        onFailure = { ImageUploadValidationResult.InvalidFile },
    )
}

private const val maxImageUploadBytes = 512_000
private val supportedImageMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
