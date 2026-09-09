package org.shareat.feature.lastactivity.domain

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.repository.RepositoryResult

fun interface GetLastActivityUseCase {
    suspend operator fun invoke(accountId: AccountId): RepositoryResult<List<LastActivityItem>>
}
