@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.shareat.feature.profile.ui.onboarding

import js.array.toJsArray
import js.reflect.unsafeCast
import js.typedarrays.toUint8Array
import web.blob.Blob
import web.blob.BlobPart
import web.blob.byteArray
import web.canvas.CanvasRenderingContext2D
import web.canvas.ID
import web.dom.document
import web.html.HTMLCanvasElement
import web.html.toBlob
import web.images.createImageBitmap
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

/** Web [actual] implementation that compresses images with the browser canvas APIs. */
actual class FileKitRestaurantImageProcessor : RestaurantImageProcessor {
    actual constructor()

    override suspend fun invoke(
        displayName: String,
        bytes: ByteArray,
    ): Result<ProcessedRestaurantImage> = try {
        require(bytes.isNotEmpty()) { "La imagen está vacía." }
        val mimeType = detectRestaurantImageMimeType(bytes)
            ?: throw IllegalArgumentException("Selecciona una imagen JPEG, PNG o WebP.")
        if (bytes.size <= MAX_RESTAURANT_IMAGE_BYTES) {
            Result.success(ProcessedRestaurantImage(displayName, bytes, mimeType))
        } else {
            val source = Blob(listOf<BlobPart>(bytes.toUint8Array()).toJsArray())
            val bitmap = createImageBitmap(source)
            try {
                val compressed = compressionAttempts.firstNotNullOfOrNull { attempt ->
                    val scale = minOf(1.0, attempt.maxDimension.toDouble() / maxOf(bitmap.width, bitmap.height))
                    val canvas = document.createElement("canvas").unsafeCast<HTMLCanvasElement>()
                    canvas.width = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
                    canvas.height = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
                    val context = requireNotNull(canvas.getContext(CanvasRenderingContext2D.ID))
                    context.drawImage(bitmap, 0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
                    canvas.toBlob("image/jpeg", attempt.quality).byteArray()
                        .takeIf { it.size <= MAX_RESTAURANT_IMAGE_BYTES }
                } ?: throw IllegalArgumentException("No se pudo reducir la imagen por debajo de 500 KB.")
                Result.success(ProcessedRestaurantImage(displayName, compressed, "image/jpeg"))
            } finally {
                bitmap.close()
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

private val compressionAttempts = listOf(
    CompressionAttempt(0.82, 1600),
    CompressionAttempt(0.70, 1280),
    CompressionAttempt(0.58, 1024),
    CompressionAttempt(0.45, 800),
)

private data class CompressionAttempt(val quality: Double, val maxDimension: Int)
