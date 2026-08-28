package org.shareat.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.auth.SessionCoordinator
import org.shareat.app.auth.RestaurantProfileCoordinator
import org.shareat.app.data.supabaseDataModule
import org.shareat.app.data.fakeDataModule
import org.shareat.app.navigation.Navigator

private val applicationModule: Module = module {
    includes(navigationModule)
    single { SessionCoordinator(get()) }
    single { RestaurantProfileCoordinator(get(), get(), get(), get()) }
    factory { parameters -> Navigator(parameters.get(), get(), get()) }
}

val sharedModule: Module = module {
    includes(supabaseDataModule(), applicationModule)
}

/** Preview/test graph. Runtime apps use [sharedModule], which is Supabase-backed. */
val previewSharedModule: Module = module {
    includes(fakeDataModule, applicationModule)
}

expect val platformModule: Module
