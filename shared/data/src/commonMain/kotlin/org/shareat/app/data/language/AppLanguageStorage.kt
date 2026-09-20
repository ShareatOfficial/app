package org.shareat.app.data.language

/** Platform storage for the selected language tag. A null value means "follow the system". */
interface AppLanguageStorage {
    fun load(): String?
    fun save(languageTag: String?)
}

/** Fallback for platforms that cannot honour an in-app choice, so nothing is persisted either. */
object NoAppLanguageStorage : AppLanguageStorage {
    override fun load(): String? = null
    override fun save(languageTag: String?) = Unit
}
