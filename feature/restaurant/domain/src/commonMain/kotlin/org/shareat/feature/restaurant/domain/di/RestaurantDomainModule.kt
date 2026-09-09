package org.shareat.feature.restaurant.domain.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.domain.usecase.di.sharedDomainModule
import org.shareat.feature.restaurant.domain.DishMatchesFiltersUseCase
import org.shareat.feature.restaurant.domain.DishMatchesFiltersUseCaseImpl

val restaurantDomainModule: Module = module {
    includes(sharedDomainModule)

    factory<DishMatchesFiltersUseCase> { DishMatchesFiltersUseCaseImpl() }
}
