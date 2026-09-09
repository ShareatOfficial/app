package org.shareat.app.navigation.profile

import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.terms.TermsAndConditionsNavigation

class TermsAndConditionsNavigationImpl(
    private val navigator: Navigator,
) : TermsAndConditionsNavigation {
    override fun goBack() {
        navigator.goBack()
    }
}
