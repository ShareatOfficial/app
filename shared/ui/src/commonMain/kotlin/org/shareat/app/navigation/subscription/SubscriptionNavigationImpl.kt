package org.shareat.app.navigation.subscription

import org.shareat.app.navigation.Navigator
import org.shareat.feature.subscription.ui.SubscriptionNavigation

class SubscriptionNavigationImpl(
    private val navigator: Navigator,
) : SubscriptionNavigation {
    override fun goBack() = navigator.goBack()
}
