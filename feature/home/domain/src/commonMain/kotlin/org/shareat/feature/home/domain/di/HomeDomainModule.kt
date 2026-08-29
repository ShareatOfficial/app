package org.shareat.feature.home.domain.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.feature.home.domain.GetRestaurantsUseCase
import org.shareat.feature.home.domain.GetRestaurantsUseCaseImpl

val homeDomainModule: Module = module {
    factory<GetRestaurantsUseCase> { GetRestaurantsUseCaseImpl(get(), get(), get()) }
}
