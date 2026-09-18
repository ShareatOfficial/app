package org.shareat.feature.restauranthome.ui.model

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageUploadPreparationTest {
    @Test
    fun uploadsTheFirstCompressionThatFitsAsJpeg() = runTest {
        val requested = mutableListOf<ImageCompression>()

        val result = preparedImageUpload(fileName = "foto.heic") { compression ->
            requested += compression
            if (compression == ImageCompressionSteps.first()) ByteArray(600_000) else ByteArray(300_000)
        }

        val upload = assertIs<ImageUploadValidationResult.Success>(result).upload
        assertEquals("image/jpeg", upload.mimeType)
        assertEquals(300_000, upload.bytes.size)
        assertEquals(ImageCompressionSteps.take(2), requested)
    }

    @Test
    fun reportsTooLargeWhenEveryCompressionStepExceedsTheBucketLimit() = runTest {
        val result = preparedImageUpload(fileName = "enorme.png") { ByteArray(512_001) }

        assertIs<ImageUploadValidationResult.TooLarge>(result)
    }

    @Test
    fun reportsInvalidFileWhenTheImageCannotBeDecoded() = runTest {
        val result = preparedImageUpload(fileName = "rota.jpg") { error("decode failed") }

        assertIs<ImageUploadValidationResult.InvalidFile>(result)
    }
}
