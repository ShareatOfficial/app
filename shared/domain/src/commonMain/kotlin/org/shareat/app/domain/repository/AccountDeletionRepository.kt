package org.shareat.app.domain.repository

interface AccountDeletionRepository {
    suspend fun requestAccountDeletion(): RepositoryResult<Unit>
}
