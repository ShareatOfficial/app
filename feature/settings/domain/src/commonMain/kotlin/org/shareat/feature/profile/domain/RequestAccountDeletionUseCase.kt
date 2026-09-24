package org.shareat.feature.profile.domain

import org.shareat.app.domain.repository.RepositoryResult

fun interface RequestAccountDeletionUseCase {
    suspend operator fun invoke(): RepositoryResult<Unit>
}
