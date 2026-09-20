package org.shareat.app.domain.model

/** The language the app renders in. [System] defers to the platform locale. */
enum class AppLanguage(val languageTag: String?) {
    System(null),
    English("en"),
    Spanish("es"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.languageTag != null && it.languageTag == tag } ?: System
    }
}

/** What a platform can do with an explicit language choice. */
enum class AppLanguageSelectionSupport {
    /** The choice applies to the running app straight away. */
    IMMEDIATE,

    /** The choice is stored but the platform only picks it up on the next launch. */
    NEXT_LAUNCH,

    /** The platform decides the language and an in-app choice cannot override it. */
    UNSUPPORTED,
}
