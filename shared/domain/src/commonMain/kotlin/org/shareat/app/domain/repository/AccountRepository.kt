package org.shareat.app.domain.repository

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.CustomerProfile

interface AccountRepository {
    suspend fun getAccount(id: AccountId): RepositoryResult<Account>
    suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile>
    suspend fun updateCustomerProfile(profile: CustomerProfile): RepositoryResult<CustomerProfile>
}
