package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.navigation.ProfileNavigation

class ProfileNavigationImpl(
    private val navigator: Navigator,
) : ProfileNavigation {
    override fun openEditProfile() {
        navigator.navigate(EditProfileKey)
    }
}
