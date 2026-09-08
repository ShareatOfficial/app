package org.shareat.app.navigation.subscription

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.app.navscenedecorator.HIDE_NAVIGATION_METADATA
import org.shareat.feature.subscription.ui.SubscriptionKey
import org.shareat.feature.subscription.ui.SubscriptionNavigation
import org.shareat.feature.subscription.ui.SubscriptionScreen
import org.shareat.feature.subscription.ui.subscriptionUiModule

@OptIn(KoinExperimentalAPI::class)
val subscriptionNavigationModule = module {
    includes(subscriptionUiModule)
    factory<SubscriptionNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        SubscriptionNavigationImpl(navigator)
    }
    navigation<SubscriptionKey>(
        metadata = mapOf(HIDE_NAVIGATION_METADATA to true),
    ) { SubscriptionScreen() }
}
