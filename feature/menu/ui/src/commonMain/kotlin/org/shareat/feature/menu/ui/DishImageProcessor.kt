package org.shareat.feature.menu.ui

/** A compressed, supported image ready for the shared [ImageUpload] contract. */
data class ProcessedDishImage(
    val displayName: String,
    val bytes: ByteArray,
    val mimeType: String,
)

/**
 * Shared contract for validating and reducing optional dish photos before upload.
 *
 * The [FileKitDishImageProcessor] `expect` declaration lets the common editor use one processor
 * while each platform supplies its own `actual` implementation. FileKit provides both the
 * Compose Multiplatform picker used by the editor and image compression on its supported native
 * targets; the web implementation uses browser canvas compression instead.
 */
fun interface DishImageProcessor {
    suspend operator fun invoke(displayName: String, bytes: ByteArray): Result<ProcessedDishImage>
}

/** Platform-specific implementation selected by Kotlin Multiplatform for the running target. */
expect class FileKitDishImageProcessor() : DishImageProcessor

internal const val MAX_DISH_IMAGE_BYTES = 512_000

internal fun detectDishImageMimeType(bytes: ByteArray): String? = when {
    bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
    bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) -> "image/png"
    bytes.size >= 12 && bytes.decodeToString(0, 4) == "RIFF" && bytes.decodeToString(8, 12) == "WEBP" -> "image/webp"
    else -> null
}
