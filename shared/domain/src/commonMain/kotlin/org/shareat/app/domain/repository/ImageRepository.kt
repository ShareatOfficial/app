package org.shareat.app.domain.repository

import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload

interface ImageRepository {
    suspend fun replaceImage(target: ImageTarget, upload: ImageUpload): RepositoryResult<ImageRef>
    suspend fun deleteImage(target: ImageTarget): RepositoryResult<Unit>
}
