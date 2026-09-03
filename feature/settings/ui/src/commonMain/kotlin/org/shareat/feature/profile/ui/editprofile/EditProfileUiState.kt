package org.shareat.feature.profile.ui.editprofile

import org.shareat.app.domain.model.AccountId

data class EditProfileUiState(
    val accountId: AccountId? = null,
    val fullName: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val preferredLanguage: ProfileLanguage = ProfileLanguage.English,
    val isEmailVerified: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val errorMessage: String? = null,
) {
    val initials: String
        get() = fullName.toInitials()

    val canSave: Boolean
        get() = accountId != null &&
            fullName.isNotBlank() &&
            displayName.isNotBlank() &&
            !isLoading &&
            !isSaving
}

enum class ProfileLanguage(
    val code: String,
    val label: String,
) {
    English("en-US", "English (US)"),
    Spanish("es-ES", "Español"),
    French("fr-FR", "Français"),
    ;

    companion object {
        fun fromCode(code: String): ProfileLanguage = entries.firstOrNull { it.code == code }
            ?: English
    }
}

private fun String.toInitials(): String = trim()
    .split(Regex("\\s+"))
    .filter(String::isNotBlank)
    .take(2)
    .mapNotNull(String::firstOrNull)
    .joinToString("")
    .uppercase()
    .ifBlank { "?" }
