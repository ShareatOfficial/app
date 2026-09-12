package org.shareat.app.navigation.profile

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.compose.koinInject
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
import org.shareat.feature.profile.ui.terms.TermsAndConditionsKey
import org.shareat.feature.profile.ui.terms.TermsAndConditionsNavigation
import org.shareat.feature.profile.ui.terms.TermsAndConditionsScreen
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation
import org.shareat.app.navscenedecorator.HIDE_NAVIGATION_METADATA

@OptIn(KoinExperimentalAPI::class)
val profileNavigationModule = module {
    includes(profileUiModule)

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
    factory<TermsAndConditionsNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        TermsAndConditionsNavigationImpl(navigator = navigator)
    }
    // Kept solely to satisfy the compiled legacy screen. It has no registered navigation route.
    factory<RestaurantOnboardingNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        RestaurantOnboardingNavigationImpl(navigator, get())
    }
    navigation<ProfileKey> { Profile() }
    navigation<SettingsKey> { SettingsScreen() }
    navigation<TermsAndConditionsKey>(
        metadata = mapOf(HIDE_NAVIGATION_METADATA to true),
    ) {
        TermsAndConditionsScreen(onBackClick = koinInject<TermsAndConditionsNavigation>()::goBack)
    }
    navigation<EditProfileKey> { EditProfileScreen() }
}
