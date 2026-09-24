package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.login.ui.LoginKey
import org.shareat.feature.profile.ui.editprofile.EditProfileKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.profile.ui.settings.SettingsNavigation
import org.shareat.feature.profile.ui.terms.TermsAndConditionsKey
import org.shareat.feature.subscription.ui.SubscriptionKey

class SettingsNavigationImpl(
    private val navigator: Navigator,
) : SettingsNavigation {
    override fun goBack() {
        navigator.goBack()
    }

    override fun openLogin() {
        navigator.navigate(LoginKey(SettingsKey))
    }

    override fun openEditProfile() {
        navigator.navigate(EditProfileKey)
    }

    override fun openSubscription() {
        navigator.navigate(SubscriptionKey)
    }

    override fun openTermsAndConditions() {
        navigator.navigate(TermsAndConditionsKey)
    }

    override fun onLogoutSuccess() {
        navigator.goHome()
    }
}
