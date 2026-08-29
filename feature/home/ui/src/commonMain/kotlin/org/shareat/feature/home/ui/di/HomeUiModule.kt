package org.shareat.feature.home.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.home.domain.di.homeDomainModule
import org.shareat.feature.home.ui.home.HomeViewModel

val homeUiModule: Module = module {
    includes(homeDomainModule)

    viewModel { HomeViewModel(get()) }
}
