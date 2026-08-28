package org.shareat.app.navigation.profile

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.di.profileUiModule
import org.shareat.feature.profile.ui.profile.Profile
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.feature.profile.ui.profile.ProfileNavigation
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.profile.ui.settings.SettingsNavigation
import org.shareat.feature.profile.ui.settings.SettingsScreen

@OptIn(KoinExperimentalAPI::class)
val profileNavigationModule = module {
    includes(profileUiModule)

    factory<ProfileNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        ProfileNavigationImpl(navigator = navigator)
    }
    factory<SettingsNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        SettingsNavigationImpl(navigator = navigator)
    }

    navigation<ProfileKey> { Profile() }
    navigation<SettingsKey> { SettingsScreen() }
}
