package org.shareat.app.data.language

import android.os.LocaleList
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import java.util.Locale

/**
 * Compose resources resolve against the process-wide default locale list, so replacing it is
 * enough for the running app — no activity recreation. The system list is captured on creation
 * because [LocaleList.getDefault] already reflects any previous override.
 */
class AndroidAppLanguageApplier(
    private val systemLocales: LocaleList = LocaleList.getDefault(),
) : AppLanguageApplier {
    override val selectionSupport = AppLanguageSelectionSupport.IMMEDIATE

    override fun apply(language: AppLanguage) {
        val locales = language.languageTag
            ?.let { LocaleList(Locale.forLanguageTag(it)) }
            ?: systemLocales
        LocaleList.setDefault(locales)
        Locale.setDefault(locales[0])
    }
}
