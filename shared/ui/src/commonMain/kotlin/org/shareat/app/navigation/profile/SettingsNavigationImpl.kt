package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.editprofile.EditProfileKey
import org.shareat.feature.profile.ui.settings.SettingsNavigation

class SettingsNavigationImpl(
    private val navigator: Navigator,
) : SettingsNavigation {
    override fun goBack() {
        navigator.goBack()
    }

    override fun openEditProfile() {
        navigator.navigate(EditProfileKey)
    }

    override fun onLogoutSuccess() {
        navigator.goHome()
    }
}
