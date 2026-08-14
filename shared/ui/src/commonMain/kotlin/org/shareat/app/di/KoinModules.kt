package org.shareat.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.data.fakeDataModule
import org.shareat.app.navigation.Navigator

val sharedModule: Module = module {
    includes(fakeDataModule)
    includes(navigationModule)
    factory { parameters -> Navigator(parameters.get()) }
}

expect val platformModule: Module
