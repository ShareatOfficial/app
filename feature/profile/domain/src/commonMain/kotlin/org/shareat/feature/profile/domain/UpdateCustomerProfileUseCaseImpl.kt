package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

data class UpdateCustomerProfileParams(
    val accountId: AccountId,
    val fullName: String,
    val displayName: String,
    val phoneNumber: String?,
    val preferredLanguage: String,
)

class UpdateCustomerProfileUseCaseImpl(
    private val accountRepository: AccountRepository,
) : UpdateCustomerProfileUseCase {
    override suspend fun invoke(
        params: UpdateCustomerProfileParams,
    ): RepositoryResult<org.shareat.app.domain.model.CustomerProfile> {
        val fullName = params.fullName.trim()
        val displayName = params.displayName.trim()
        val phoneNumber = params.phoneNumber?.trim()?.ifBlank { null }
        val preferredLanguage = params.preferredLanguage.trim()

        if (fullName.isEmpty()) {
            return RepositoryResult.Failure(RepositoryError.Validation("Full name is required."))
        }
        if (displayName.isEmpty()) {
            return RepositoryResult.Failure(
                RepositoryError.Validation("Display name is required."),
            )
        }
        if (preferredLanguage.isEmpty()) {
            return RepositoryResult.Failure(
                RepositoryError.Validation("Preferred language is required."),
            )
        }

        val current = when (val result = accountRepository.getCustomerProfile(params.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return accountRepository.updateCustomerProfile(
            current.copy(
                fullName = fullName,
                displayName = displayName,
                phoneNumber = phoneNumber,
                preferredLanguage = preferredLanguage,
            ),
        )
    }
}
