package org.shareat.app.navigation.profile

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.LocalNavigator
import org.shareat.feature.profile.Profile
import org.shareat.feature.profile.ProfileDetails

@OptIn(KoinExperimentalAPI::class)
val profileNavigationModule = module {
    navigation<ProfileKey> {
        val navigator = LocalNavigator.current
        Profile(onOpenDetails = { navigator.navigate(ProfileDetailsKey) })
    }
    navigation<ProfileDetailsKey> {
        val navigator = LocalNavigator.current
        ProfileDetails(onBack = navigator::goBack)
    }
}
