package org.shareat.app.data.fake

import org.shareat.app.domain.repository.AccountDeletionRepository
import org.shareat.app.domain.repository.RepositoryResult

internal class FakeAccountDeletionRepository : AccountDeletionRepository {
    override suspend fun requestAccountDeletion(): RepositoryResult<Unit> =
        RepositoryResult.Success(Unit)
}
