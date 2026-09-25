package org.shareat.feature.restauranthome.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.restauranthome.domain.GetRestaurantHomeUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerDishUseCaseImpl
import org.shareat.feature.restauranthome.domain.CreateOwnerRestaurantUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerRestaurantUseCaseImpl
import org.shareat.feature.restauranthome.domain.GetRestaurantHomeUseCaseImpl
import org.shareat.feature.restauranthome.domain.ReplaceOwnerDishImageUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerDishImageUseCaseImpl
import org.shareat.feature.restauranthome.domain.ReplaceOwnerRestaurantImageUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerRestaurantImageUseCaseImpl
import org.shareat.feature.restauranthome.domain.RestaurantOwnerAuthorizer
import org.shareat.feature.restauranthome.domain.UpdateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerDishUseCaseImpl
import org.shareat.feature.restauranthome.domain.UpdateOwnerRestaurantInfoUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerRestaurantInfoUseCaseImpl
import org.shareat.feature.restauranthome.domain.UpdateRestaurantPublicationStateUseCase
import org.shareat.feature.restauranthome.domain.UpdateRestaurantPublicationStateUseCaseImpl
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomeViewModel

/** Koin wiring for the restaurant-owner home feature. */
val restaurantHomeUiModule: Module = module {
    factory { RestaurantOwnerAuthorizer(get(), get()) }

    factory<GetRestaurantHomeUseCase> {
        GetRestaurantHomeUseCaseImpl(get(), get(), get(), get())
    }
    factory<CreateOwnerDishUseCase> {
        CreateOwnerDishUseCaseImpl(get(), get(), get(), get())
    }
    factory<CreateOwnerRestaurantUseCase> {
        CreateOwnerRestaurantUseCaseImpl(get(), get())
    }
    factory<UpdateOwnerRestaurantInfoUseCase> {
        UpdateOwnerRestaurantInfoUseCaseImpl(get(), get())
    }
    factory<UpdateRestaurantPublicationStateUseCase> {
        UpdateRestaurantPublicationStateUseCaseImpl(get(), get())
    }
    factory<ReplaceOwnerRestaurantImageUseCase> {
        ReplaceOwnerRestaurantImageUseCaseImpl(get(), get(), get())
    }
    factory<UpdateOwnerDishUseCase> {
        UpdateOwnerDishUseCaseImpl(get(), get(), get(), get())
    }
    factory<ReplaceOwnerDishImageUseCase> {
        ReplaceOwnerDishImageUseCaseImpl(get(), get(), get(), get())
    }

    viewModel {
        RestaurantHomeViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }
}
