package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.RepositoryResult

fun interface ReplaceOwnerDishImageUseCase {
    suspend operator fun invoke(dishId: DishId, upload: ImageUpload): RepositoryResult<ImageRef>
}
