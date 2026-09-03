package org.shareat.feature.profile.domain

import org.shareat.app.domain.repository.RepositoryResult

fun interface LoadProfileSettingsUseCase {
    suspend operator fun invoke(): RepositoryResult<ProfileSettings>
}