package org.shareat.feature.home.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.home.domain.GetHomeRestaurantsUseCase
import org.shareat.feature.home.domain.GetHomeRestaurantsUseCaseImpl
import org.shareat.feature.home.ui.home.HomeViewModel

val homeUiModule: Module = module {
    factory<GetHomeRestaurantsUseCase> { GetHomeRestaurantsUseCaseImpl(get(), get(), get()) }

    viewModel { HomeViewModel(get()) }
}
