package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class CreateRestaurantProfileUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val restaurantRepository: RestaurantRepository,
) : CreateRestaurantProfileUseCase {
    override suspend fun invoke(
        restaurantProfile: RestaurantProfileDraft,
    ): RepositoryResult<org.shareat.app.domain.model.Restaurant> {
        val session = when (val result = authRepository.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val account = when (val result = accountRepository.getAccount(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (account.role != AccountRole.Restaurant || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(RepositoryError.Forbidden)
        }
        return restaurantRepository.createRestaurantProfile(
            ownerAccountId = account.id,
            draft = restaurantProfile,
        )
    }
}
