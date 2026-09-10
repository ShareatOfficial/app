package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class RestaurantOwnerAuthorizer(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
) {
    suspend fun authorize(): RepositoryResult<Account> {
        val session = when (val result = authRepository.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val account = when (val result = accountRepository.getAccount(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return if (account.role == AccountRole.Restaurant && account.status == AccountStatus.Active) {
            RepositoryResult.Success(account)
        } else {
            forbidden()
        }
    }
}

internal fun forbidden(): RepositoryResult.Failure =
    RepositoryResult.Failure(RepositoryError.Forbidden)
