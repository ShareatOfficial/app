package org.shareat.app.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport

interface AppLanguageRepository {
    /**
     * A [StateFlow] rather than a plain [kotlinx.coroutines.flow.Flow] so the composition root
     * renders the stored language on its first frame instead of briefly falling back to the
     * system one.
     */
    fun observeSelected(): StateFlow<AppLanguage>
    suspend fun selectionSupport(): AppLanguageSelectionSupport
    suspend fun select(language: AppLanguage)
}
