package org.shareat.app.language

import kotlinx.coroutines.flow.StateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.repository.AppLanguageRepository

/** Exposes the selected language to the composition root, which rebuilds its content when it changes. */
class AppLanguageCoordinator(repository: AppLanguageRepository) {
    val selected: StateFlow<AppLanguage> = repository.observeSelected()
}
