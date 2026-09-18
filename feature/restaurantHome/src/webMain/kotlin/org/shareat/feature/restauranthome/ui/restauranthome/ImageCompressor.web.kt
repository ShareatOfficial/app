@file:OptIn(ExperimentalWasmJsInterop::class)

package org.shareat.feature.restauranthome.ui.restauranthome

import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.await
import org.shareat.feature.restauranthome.ui.model.ImageCompression
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js
import kotlin.js.unsafeCast

internal actual suspend fun PlatformFile.compressedAsJpeg(compression: ImageCompression): ByteArray {
    val source = (webFile as? WebFile.FileWrapper)?.file ?: error("Only regular files can be compressed")
    val jpeg = drawAsJpeg(source, compression.maxDimension, compression.quality / 100.0).await<JsAny>()
    return PlatformFile(WebFile.FileWrapper(jpeg.unsafeCast<BrowserFile>())).readBytes()
}

// createImageBitmap honours EXIF orientation; the white fill keeps transparent PNGs from turning black.
private fun drawAsJpeg(file: JsAny, maxDimension: Int, quality: Double): Promise<JsAny> = js(
    """createImageBitmap(file).then(function (bitmap) {
        var scale = Math.min(1, maxDimension / Math.max(bitmap.width, bitmap.height));
        var canvas = document.createElement('canvas');
        canvas.width = Math.max(1, Math.round(bitmap.width * scale));
        canvas.height = Math.max(1, Math.round(bitmap.height * scale));
        var context = canvas.getContext('2d');
        context.fillStyle = '#ffffff';
        context.fillRect(0, 0, canvas.width, canvas.height);
        context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
        bitmap.close();
        return new Promise(function (resolve, reject) {
            canvas.toBlob(function (blob) {
                if (blob) resolve(new File([blob], file.name, { type: 'image/jpeg' }));
                else reject(new Error('Could not encode image'));
            }, 'image/jpeg', quality);
        });
    })"""
)
