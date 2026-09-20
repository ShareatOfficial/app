package org.shareat.app.data.language

import android.content.Context

class AndroidAppLanguageStorage(context: Context) : AppLanguageStorage {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): String? = preferences.getString(LANGUAGE_TAG_KEY, null)

    override fun save(languageTag: String?) {
        preferences.edit().apply {
            if (languageTag == null) remove(LANGUAGE_TAG_KEY) else putString(LANGUAGE_TAG_KEY, languageTag)
        }.apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "shareat_app_language"
        const val LANGUAGE_TAG_KEY = "language_tag"
    }
}
