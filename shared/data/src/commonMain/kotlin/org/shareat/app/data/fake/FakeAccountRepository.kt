package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeAccountRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
) : AccountRepository {
    override suspend fun getAccount(id: AccountId): RepositoryResult<Account> = scenario.result(
        populated = {
            data.accounts.firstOrNull { it.id == id }
                ?: return RepositoryResult.Failure(RepositoryError.NotFound("Account", id.value))
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("Account", id.value))
        },
    )

    override suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile> = scenario.result(
        populated = {
            data.customerProfiles.firstOrNull { it.accountId == accountId }
                ?: return RepositoryResult.Failure(RepositoryError.NotFound("CustomerProfile", accountId.value))
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("CustomerProfile", accountId.value))
        },
    )
}
