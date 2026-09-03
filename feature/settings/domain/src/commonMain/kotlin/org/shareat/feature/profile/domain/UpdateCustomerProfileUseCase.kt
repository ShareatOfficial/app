package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.repository.RepositoryResult

fun interface UpdateCustomerProfileUseCase {
    suspend operator fun invoke(
        params: UpdateCustomerProfileParams,
    ): RepositoryResult<CustomerProfile>
}