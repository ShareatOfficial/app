package org.shareat.feature.profile.domain

import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryResult

class SignOutUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignOutUseCase {
    override suspend fun invoke(): RepositoryResult<Unit> = authRepository.signOut()
}
