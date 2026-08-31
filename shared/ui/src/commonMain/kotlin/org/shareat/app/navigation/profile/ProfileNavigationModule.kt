package org.shareat.app.navigation.profile

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.di.profileUiModule
import org.shareat.feature.profile.ui.editprofile.EditProfileKey
import org.shareat.feature.profile.ui.editprofile.EditProfileNavigation
import org.shareat.feature.profile.ui.editprofile.EditProfileScreen
import org.shareat.feature.profile.ui.profile.Profile
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.feature.profile.ui.profile.ProfileNavigation
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.profile.ui.settings.SettingsNavigation
import org.shareat.feature.profile.ui.settings.SettingsScreen
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingKey
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingScreen
import org.shareat.app.navscenedecorator.HIDE_NAVIGATION_METADATA
import org.shareat.feature.menu.ui.MenuManagementKey
import org.shareat.feature.menu.ui.MenuManagementNavigation
import org.shareat.feature.menu.ui.MenuManagementScreen
import org.shareat.feature.menu.ui.di.menuUiModule

@OptIn(KoinExperimentalAPI::class)
val profileNavigationModule = module {
    includes(profileUiModule)
    includes(menuUiModule)

    factory<ProfileNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        ProfileNavigationImpl(navigator = navigator)
    }
    factory<EditProfileNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        EditProfileNavigationImpl(navigator = navigator)
    }
    factory<SettingsNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        SettingsNavigationImpl(navigator = navigator)
    }
    factory<RestaurantOnboardingNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        RestaurantOnboardingNavigationImpl(navigator, get())
    }
    factory<MenuManagementNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        MenuManagementNavigationImpl(navigator)
    }

    navigation<ProfileKey> { Profile() }
    navigation<SettingsKey> { SettingsScreen() }
    navigation<EditProfileKey> { EditProfileScreen() }
    navigation<RestaurantOnboardingKey>(
        metadata = mapOf(HIDE_NAVIGATION_METADATA to true),
    ) { RestaurantOnboardingScreen() }
    navigation<MenuManagementKey> { MenuManagementScreen() }
}
