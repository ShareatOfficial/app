package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.model.OwnerDishDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate

fun interface UpdateOwnerDishUseCase {
    suspend operator fun invoke(
        dishId: DishId,
        draft: OwnerDishDraft,
    ): RepositoryResult<OwnerDishUpdate>
}
