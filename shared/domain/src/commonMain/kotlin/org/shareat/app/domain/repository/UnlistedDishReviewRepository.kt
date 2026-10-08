package org.shareat.app.domain.repository

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft

interface UnlistedDishReviewRepository {
    suspend fun create(draft: UnlistedDishReviewDraft): RepositoryResult<UnlistedDishReview>
    suspend fun getByAuthor(accountId: AccountId): RepositoryResult<List<UnlistedDishReview>>
}
