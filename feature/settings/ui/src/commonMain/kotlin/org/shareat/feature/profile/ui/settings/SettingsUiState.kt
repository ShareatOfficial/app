package org.shareat.feature.profile.ui.settings

import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport

sealed interface SettingsUiState {
    val isLoading: Boolean
    val error: SettingsError?
    val language: AppLanguageUiState

    data class User(
        val name: String = "Alex Rivera",
        val email: String = "alex.rivera@example.com",
        val initials: String = "AR",
        override val isLoading: Boolean = false,
        override val error: SettingsError? = null,
        override val language: AppLanguageUiState = AppLanguageUiState(),
    ) : SettingsUiState

    data class Restaurant(
        val name: String = "Osteria Bella",
        val description: String =
            "A contemporary approach to traditional Italian family recipes.",
        val phone: String = "+1 (555) 123-4567",
        val email: String = "hello@osteriabella.com",
        val streetAddress: String = "1284 Culinary Blvd",
        val city: String = "Portland",
        val postcode: String = "97205",
        val isPublished: Boolean = true,
        val openingHours: List<OpeningHoursUiState> = defaultOpeningHours(),
        override val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val saveSucceeded: Boolean = false,
        override val error: SettingsError? = null,
        override val language: AppLanguageUiState = AppLanguageUiState(),
    ) : SettingsUiState
}

internal fun SettingsUiState.withLanguage(language: AppLanguageUiState): SettingsUiState =
    when (this) {
        is SettingsUiState.User -> copy(language = language)
        is SettingsUiState.Restaurant -> copy(language = language)
    }

data class AppLanguageUiState(
    val selected: AppLanguage = AppLanguage.System,
    val support: AppLanguageSelectionSupport = AppLanguageSelectionSupport.IMMEDIATE,
) {
    val canSelect: Boolean get() = support != AppLanguageSelectionSupport.UNSUPPORTED
}

enum class OpeningDay {
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
}

data class OpeningHoursUiState(
    val day: OpeningDay,
    val isOpen: Boolean,
    val openingTime: String = "11:00",
    val closingTime: String = "22:00",
)

private fun defaultOpeningHours() = OpeningDay.entries.map { day ->
    OpeningHoursUiState(
        day = day,
        isOpen = true,
        closingTime = when (day) {
            OpeningDay.Friday, OpeningDay.Saturday -> "23:00"
            OpeningDay.Sunday -> "21:00"
            else -> "22:00"
        },
    )
}
