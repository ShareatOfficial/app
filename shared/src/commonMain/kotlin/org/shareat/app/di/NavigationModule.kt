package org.shareat.app.di

import org.koin.dsl.module
import org.shareat.app.navigation.home.homeNavigationModule
import org.shareat.app.navigation.login.loginNavigationModule
import org.shareat.app.navigation.profile.profileNavigationModule

val navigationModule = module {
    includes(
        homeNavigationModule,
        profileNavigationModule,
        loginNavigationModule,
    )
}
