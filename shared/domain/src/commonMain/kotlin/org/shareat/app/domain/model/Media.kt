package org.shareat.app.domain.model

sealed interface ImageTarget {
    data class CustomerAvatar(val accountId: AccountId) : ImageTarget
    data class RestaurantHero(val restaurantId: RestaurantId) : ImageTarget
    data class DishImage(val dishId: DishId) : ImageTarget
}

data class ImageUpload(
    val bytes: ByteArray,
    val mimeType: String,
    val alternativeText: String? = null,
) {
    init {
        require(bytes.isNotEmpty())
        require(bytes.size <= 512_000)
        require(mimeType in SupportedImageMimeTypes)
        require(alternativeText == null || alternativeText.isNotBlank())
    }

    override fun equals(other: Any?): Boolean =
        other is ImageUpload &&
            bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            alternativeText == other.alternativeText

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + mimeType.hashCode()

    private companion object {
        val SupportedImageMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
    }
}
