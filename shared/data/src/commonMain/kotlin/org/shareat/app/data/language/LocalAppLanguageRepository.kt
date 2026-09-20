package org.shareat.app.data.language

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import org.shareat.app.domain.repository.AppLanguageRepository

/**
 * Constructing this repository re-applies the stored choice, so the platform locale is already
 * correct by the time the first composable resolves a string resource.
 */
class LocalAppLanguageRepository(
    private val storage: AppLanguageStorage = NoAppLanguageStorage,
    private val applier: AppLanguageApplier = UnsupportedAppLanguageApplier,
) : AppLanguageRepository {
    private val selected = MutableStateFlow(AppLanguage.fromTag(storage.load()))

    init {
        applier.apply(selected.value)
    }

    override fun observeSelected(): StateFlow<AppLanguage> = selected.asStateFlow()

    override suspend fun selectionSupport(): AppLanguageSelectionSupport = applier.selectionSupport

    override suspend fun select(language: AppLanguage) {
        if (applier.selectionSupport == AppLanguageSelectionSupport.UNSUPPORTED) return
        storage.save(language.languageTag)
        applier.apply(language)
        selected.value = language
    }
}
