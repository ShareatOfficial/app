package org.shareat.shared.media

import io.github.vinceglb.filekit.PlatformFile

public data class ImageCompression(public val maxDimension: Int, public val quality: Int)

public expect suspend fun PlatformFile.compressedAsJpeg(compression: ImageCompression): ByteArray
