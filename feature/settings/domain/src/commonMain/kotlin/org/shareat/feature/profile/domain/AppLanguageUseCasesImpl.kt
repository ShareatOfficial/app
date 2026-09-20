package org.shareat.feature.profile.domain

import kotlinx.coroutines.flow.StateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import org.shareat.app.domain.repository.AppLanguageRepository

class ObserveAppLanguageUseCaseImpl(
    private val appLanguageRepository: AppLanguageRepository,
) : ObserveAppLanguageUseCase {
    override fun invoke(): StateFlow<AppLanguage> = appLanguageRepository.observeSelected()
}

class GetAppLanguageSupportUseCaseImpl(
    private val appLanguageRepository: AppLanguageRepository,
) : GetAppLanguageSupportUseCase {
    override suspend fun invoke(): AppLanguageSelectionSupport =
        appLanguageRepository.selectionSupport()
}

class SelectAppLanguageUseCaseImpl(
    private val appLanguageRepository: AppLanguageRepository,
) : SelectAppLanguageUseCase {
    override suspend fun invoke(language: AppLanguage) = appLanguageRepository.select(language)
}
