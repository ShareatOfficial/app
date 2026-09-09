package org.shareat.app.di

import org.koin.dsl.module
import org.shareat.app.navigation.home.homeNavigationModule
import org.shareat.app.navigation.login.loginNavigationModule
import org.shareat.app.navigation.lastactivity.lastActivityNavigationModule
import org.shareat.app.navigation.profile.profileNavigationModule
import org.shareat.app.navigation.restaurant.restaurantNavigationModule
import org.shareat.app.navigation.subscription.subscriptionNavigationModule

val navigationModule = module {
    includes(
        homeNavigationModule,
        lastActivityNavigationModule,
        profileNavigationModule,
        restaurantNavigationModule,
        loginNavigationModule,
        subscriptionNavigationModule,
    )
}
