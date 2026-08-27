package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.navigation.EditProfileKey
import org.shareat.feature.profile.ui.navigation.ProfileNavigation

class ProfileNavigationImpl(
    private val navigator: Navigator,
) : ProfileNavigation {
    override fun openEditProfile() {
        navigator.navigate(EditProfileKey)
    }
}
