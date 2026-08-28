package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class LoadProfileSettingsUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val restaurantRepository: RestaurantRepository,
) : LoadProfileSettingsUseCase {
    override suspend fun invoke(): RepositoryResult<ProfileSettings> {
        val session = when (val result = authRepository.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val account = when (val result = accountRepository.getAccount(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }

        return when (account.role) {
            AccountRole.Customer -> when (
                val result = accountRepository.getCustomerProfile(account.id)
            ) {
                is RepositoryResult.Success -> RepositoryResult.Success(
                    ProfileSettings.User(account, result.value),
                )
                is RepositoryResult.Failure -> result
            }

            AccountRole.Restaurant -> when (
                val result = restaurantRepository.getRestaurantForOwner(account.id)
            ) {
                is RepositoryResult.Success -> RepositoryResult.Success(
                    ProfileSettings.RestaurantOwner(account, result.value),
                )
                is RepositoryResult.Failure -> result
            }
        }
    }
}
