package org.shareat.feature.profile.ui.onboarding

fun interface RestaurantImageProcessor {
    suspend operator fun invoke(
        displayName: String,
        bytes: ByteArray,
    ): Result<ProcessedRestaurantImage>
}

expect class FileKitRestaurantImageProcessor() : RestaurantImageProcessor

internal fun detectRestaurantImageMimeType(bytes: ByteArray): String? = when {
    bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() &&
        bytes[2] == 0xFF.toByte() -> "image/jpeg"
    bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    ) -> "image/png"
    bytes.size >= 12 && bytes.decodeToString(0, 4) == "RIFF" &&
        bytes.decodeToString(8, 12) == "WEBP" -> "image/webp"
    else -> null
}

internal const val MAX_RESTAURANT_IMAGE_BYTES = 512_000
