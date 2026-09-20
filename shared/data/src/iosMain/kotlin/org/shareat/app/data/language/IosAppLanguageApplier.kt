package org.shareat.app.data.language

import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport

/**
 * iOS resolves `NSLocale.preferredLanguages` once per process, so the stored choice only reaches
 * Compose resources on the next launch. Persisting it is the whole of the work; there is nothing
 * to apply to the running app.
 */
class IosAppLanguageApplier : AppLanguageApplier {
    override val selectionSupport = AppLanguageSelectionSupport.NEXT_LAUNCH

    override fun apply(language: AppLanguage) = Unit
}
