package org.shareat.shared.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage

public actual suspend fun PlatformFile.compressedAsJpeg(compression: ImageCompression): ByteArray =
    FileKit.compressImage(
        file = this,
        imageFormat = ImageFormat.JPEG,
        quality = compression.quality,
        maxWidth = compression.maxDimension,
        maxHeight = compression.maxDimension,
    )
