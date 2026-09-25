package org.shareat.feature.profile.ui.settings

interface SettingsNavigation {
    fun goBack()
    fun openLogin()
    fun openEditProfile()
    fun openTermsAndConditions()
    fun onLogoutSuccess()
}
