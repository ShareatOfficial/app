@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.shareat.feature.menu.ui

import js.array.toJsArray
import js.reflect.unsafeCast
import js.typedarrays.toUint8Array
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt
import web.blob.Blob
import web.blob.BlobPart
import web.blob.byteArray
import web.canvas.CanvasRenderingContext2D
import web.canvas.ID
import web.dom.document
import web.html.HTMLCanvasElement
import web.html.toBlob
import web.images.createImageBitmap

/** Web `actual` counterpart of the shared `expect` processor, using browser canvas compression. */
actual class FileKitDishImageProcessor : DishImageProcessor {
    actual constructor()
    override suspend fun invoke(displayName: String, bytes: ByteArray): Result<ProcessedDishImage> = try {
        require(bytes.isNotEmpty()) { "The image is empty." }
        val mime = detectDishImageMimeType(bytes) ?: throw IllegalArgumentException("Select a JPEG, PNG, or WebP image.")
        if (bytes.size <= MAX_DISH_IMAGE_BYTES) Result.success(ProcessedDishImage(displayName, bytes, mime)) else {
            val bitmap = createImageBitmap(Blob(listOf<BlobPart>(bytes.toUint8Array()).toJsArray()))
            try {
                val compressed = attempts.firstNotNullOfOrNull { attempt ->
                    val scale = minOf(1.0, attempt.dimension.toDouble() / maxOf(bitmap.width, bitmap.height))
                    val canvas = document.createElement("canvas").unsafeCast<HTMLCanvasElement>()
                    canvas.width = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
                    canvas.height = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
                    requireNotNull(canvas.getContext(CanvasRenderingContext2D.ID)).drawImage(bitmap, 0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
                    canvas.toBlob("image/jpeg", attempt.quality).byteArray().takeIf { it.size <= MAX_DISH_IMAGE_BYTES }
                } ?: throw IllegalArgumentException("The image could not be reduced below 500 KB.")
                Result.success(ProcessedDishImage(displayName, compressed, "image/jpeg"))
            } finally { bitmap.close() }
        }
    } catch (error: CancellationException) { throw error } catch (error: Throwable) { Result.failure(error) }
}

private data class Attempt(val quality: Double, val dimension: Int)
private val attempts = listOf(Attempt(0.82, 1600), Attempt(0.70, 1280), Attempt(0.58, 1024), Attempt(0.45, 800))
