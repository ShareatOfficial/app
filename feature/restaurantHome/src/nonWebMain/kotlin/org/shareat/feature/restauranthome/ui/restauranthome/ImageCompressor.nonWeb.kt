package org.shareat.feature.restauranthome.ui.restauranthome

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import org.shareat.feature.restauranthome.ui.model.ImageCompression

internal actual suspend fun PlatformFile.compressedAsJpeg(compression: ImageCompression): ByteArray =
    FileKit.compressImage(
        file = this,
        imageFormat = ImageFormat.JPEG,
        quality = compression.quality,
        maxWidth = compression.maxDimension,
        maxHeight = compression.maxDimension,
    )
