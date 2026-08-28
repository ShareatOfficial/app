package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.profile.ProfileNavigation
import org.shareat.feature.profile.ui.settings.SettingsKey

class ProfileNavigationImpl(
    private val navigator: Navigator,
) : ProfileNavigation {
    override fun openSettings() {
        navigator.navigate(SettingsKey)
    }
}
