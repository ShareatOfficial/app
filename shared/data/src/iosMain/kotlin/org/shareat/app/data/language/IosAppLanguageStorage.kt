package org.shareat.app.data.language

import platform.Foundation.NSUserDefaults

/**
 * Writes both our own key and `AppleLanguages`, the list UIKit reads when it resolves the bundle
 * locale on launch.
 */
class IosAppLanguageStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : AppLanguageStorage {
    override fun load(): String? = defaults.stringForKey(LANGUAGE_TAG_KEY)

    override fun save(languageTag: String?) {
        if (languageTag == null) {
            defaults.removeObjectForKey(LANGUAGE_TAG_KEY)
            defaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
        } else {
            defaults.setObject(languageTag, LANGUAGE_TAG_KEY)
            defaults.setObject(listOf(languageTag), APPLE_LANGUAGES_KEY)
        }
        defaults.synchronize()
    }

    private companion object {
        const val LANGUAGE_TAG_KEY = "shareat_language_tag"
        const val APPLE_LANGUAGES_KEY = "AppleLanguages"
    }
}
