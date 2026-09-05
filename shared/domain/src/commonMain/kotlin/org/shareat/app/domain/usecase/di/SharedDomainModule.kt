package org.shareat.app.domain.usecase.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.domain.usecase.GetRestaurantUseCase
import org.shareat.app.domain.usecase.GetRestaurantUseCaseImpl
import org.shareat.app.domain.usecase.GetRestaurantsUseCase
import org.shareat.app.domain.usecase.GetRestaurantsUseCaseImpl
import org.shareat.app.domain.usecase.RestaurantDetailsAssembler

val sharedDomainModule: Module = module {
    factory { RestaurantDetailsAssembler(get(), get(), get()) }
    factory<GetRestaurantsUseCase> { GetRestaurantsUseCaseImpl(get(), get()) }
    factory<GetRestaurantUseCase> { GetRestaurantUseCaseImpl(get(), get()) }
}
