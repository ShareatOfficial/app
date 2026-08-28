package org.shareat.feature.profile.ui.editprofile

sealed interface EditProfileAction {
    data class FullNameChanged(val value: String) : EditProfileAction
    data class DisplayNameChanged(val value: String) : EditProfileAction
    data class PhoneNumberChanged(val value: String) : EditProfileAction
    data class PreferredLanguageChanged(val value: ProfileLanguage) : EditProfileAction
    data object ChangePhoto : EditProfileAction
    data object Save : EditProfileAction
}
