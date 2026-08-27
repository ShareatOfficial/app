package org.shareat.app.navigation.profile

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.profile.ui.EditProfileScreen
import org.shareat.feature.profile.ui.Profile
import org.shareat.feature.profile.ui.di.profileUiModule
import org.shareat.feature.profile.ui.navigation.EditProfileKey
import org.shareat.feature.profile.ui.navigation.EditProfileNavigation
import org.shareat.feature.profile.ui.navigation.ProfileKey
import org.shareat.feature.profile.ui.navigation.ProfileNavigation

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

    navigation<ProfileKey> { Profile() }
    navigation<EditProfileKey> { EditProfileScreen() }
}
