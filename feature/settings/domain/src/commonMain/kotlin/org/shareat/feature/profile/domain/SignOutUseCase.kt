package org.shareat.feature.profile.domain

import org.shareat.app.domain.repository.RepositoryResult

fun interface SignOutUseCase {
    suspend operator fun invoke(): RepositoryResult<Unit>
}
