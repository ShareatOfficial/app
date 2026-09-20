package org.shareat.app.data.language

import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport

/** Applies a language to the platform locale that Compose resources resolve against. */
interface AppLanguageApplier {
    val selectionSupport: AppLanguageSelectionSupport
    fun apply(language: AppLanguage)
}

/** The browser decides the language of a web build and a page cannot override it. */
object UnsupportedAppLanguageApplier : AppLanguageApplier {
    override val selectionSupport = AppLanguageSelectionSupport.UNSUPPORTED
    override fun apply(language: AppLanguage) = Unit
}
