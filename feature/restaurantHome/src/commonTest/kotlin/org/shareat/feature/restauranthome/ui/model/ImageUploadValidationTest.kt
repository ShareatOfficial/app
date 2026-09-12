package org.shareat.feature.restauranthome.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageUploadValidationTest {
    @Test
    fun acceptsSupportedMimeTypesAndUsesTheFileNameAsAlternativeText() {
        val result = imageUploadFrom(
            bytes = byteArrayOf(1, 2),
            mimeType = "image/PNG",
            fileName = "plato.png",
        )

        val upload = assertIs<ImageUploadValidationResult.Success>(result).upload
        assertEquals("image/png", upload.mimeType)
        assertEquals("plato.png", upload.alternativeText)
    }

    @Test
    fun rejectsUnsupportedMimeTypesAndOversizedFiles() {
        assertIs<ImageUploadValidationResult.UnsupportedFormat>(
            imageUploadFrom(byteArrayOf(1), "image/gif", "animacion.gif"),
        )
        assertIs<ImageUploadValidationResult.TooLarge>(
            imageUploadFrom(ByteArray(512_001), "image/jpeg", "grande.jpg"),
        )
        assertIs<ImageUploadValidationResult.InvalidFile>(
            imageUploadFrom(byteArrayOf(), "image/webp", "vacia.webp"),
        )
    }
}
