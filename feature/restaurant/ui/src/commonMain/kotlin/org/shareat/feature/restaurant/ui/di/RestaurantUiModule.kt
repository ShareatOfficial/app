package org.shareat.feature.restaurant.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.restaurant.domain.di.restaurantDomainModule
import org.shareat.feature.restaurant.ui.restaurant.RestaurantViewModel

val restaurantUiModule: Module = module {
    includes(restaurantDomainModule)

    viewModel { parameters -> RestaurantViewModel(parameters.get(), get(), get()) }
}
