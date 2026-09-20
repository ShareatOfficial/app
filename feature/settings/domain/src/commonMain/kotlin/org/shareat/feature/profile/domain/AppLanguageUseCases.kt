package org.shareat.feature.profile.domain

import kotlinx.coroutines.flow.StateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport

fun interface ObserveAppLanguageUseCase {
    operator fun invoke(): StateFlow<AppLanguage>
}

fun interface GetAppLanguageSupportUseCase {
    suspend operator fun invoke(): AppLanguageSelectionSupport
}

fun interface SelectAppLanguageUseCase {
    suspend operator fun invoke(language: AppLanguage)
}
