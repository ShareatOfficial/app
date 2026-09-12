package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate

/** Creates a dish owned by the current restaurant and appends it to its sole menu. */
fun interface CreateOwnerDishUseCase {
    suspend operator fun invoke(draft: OwnerDishCreateDraft): RepositoryResult<OwnerDishUpdate>
}
