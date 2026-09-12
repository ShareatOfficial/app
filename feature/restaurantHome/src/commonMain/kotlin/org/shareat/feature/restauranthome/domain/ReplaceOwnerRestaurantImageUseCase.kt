package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.RepositoryResult

fun interface ReplaceOwnerRestaurantImageUseCase {
    suspend operator fun invoke(upload: ImageUpload): RepositoryResult<ImageRef>
}
