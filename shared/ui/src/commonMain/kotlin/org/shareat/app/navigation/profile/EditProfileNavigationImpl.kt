package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.editprofile.EditProfileNavigation

class EditProfileNavigationImpl(
    private val navigator: Navigator,
) : EditProfileNavigation {
    override fun goBack() {
        navigator.goBack()
    }
}
