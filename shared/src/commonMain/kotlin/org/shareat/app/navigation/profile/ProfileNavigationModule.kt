package org.shareat.app.navigation.profile

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.LocalNavigator
import org.shareat.feature.profile.EditProfile
import org.shareat.feature.profile.Profile
import org.shareat.feature.profile.navigation.EditProfileNavigation
import org.shareat.feature.profile.navigation.ProfileNavigation

@OptIn(KoinExperimentalAPI::class)
val profileNavigationModule = module {
    factory<ProfileNavigation> { parameters ->
        ProfileNavigationImpl(navigator = parameters.get())
    }
    factory<EditProfileNavigation> { parameters ->
        EditProfileNavigationImpl(navigator = parameters.get())
    }

    navigation<ProfileKey> {
        val appNavigator = LocalNavigator.current
        val profileNavigator = koinInject<ProfileNavigation> {
            parametersOf(appNavigator)
        }

        Profile(navigator = profileNavigator)
    }
    navigation<EditProfileKey> {
        val appNavigator = LocalNavigator.current
        val editProfileNavigator = koinInject<EditProfileNavigation> {
            parametersOf(appNavigator)
        }

        EditProfile(navigator = editProfileNavigator)
    }
}
