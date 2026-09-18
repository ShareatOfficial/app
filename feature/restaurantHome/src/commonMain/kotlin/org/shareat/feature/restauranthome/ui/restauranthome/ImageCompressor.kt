package org.shareat.feature.restauranthome.ui.restauranthome

import io.github.vinceglb.filekit.PlatformFile
import org.shareat.feature.restauranthome.ui.model.ImageCompression

internal expect suspend fun PlatformFile.compressedAsJpeg(compression: ImageCompression): ByteArray
