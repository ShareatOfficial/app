package org.shareat.app.navigation.lastactivity

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.lastactivity.di.lastActivityModule
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.lastactivity.ui.LastActivityNavigation
import org.shareat.feature.lastactivity.ui.LastActivityScreen

@OptIn(KoinExperimentalAPI::class)
val lastActivityNavigationModule = module {
    includes(lastActivityModule)
    factory<LastActivityNavigation> { parameters ->
        LastActivityNavigationImpl(parameters.getOrNull<Navigator>() ?: get<Navigator>())
    }
    navigation<LastActivityKey> { LastActivityScreen() }
}
