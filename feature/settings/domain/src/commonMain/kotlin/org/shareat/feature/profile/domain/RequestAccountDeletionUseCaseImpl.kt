package org.shareat.feature.profile.domain

import org.shareat.app.domain.repository.AccountDeletionRepository
import org.shareat.app.domain.repository.RepositoryResult

class RequestAccountDeletionUseCaseImpl(
    private val repository: AccountDeletionRepository,
) : RequestAccountDeletionUseCase {
    override suspend fun invoke(): RepositoryResult<Unit> = repository.requestAccountDeletion()
}
