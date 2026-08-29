package org.shareat.feature.menu.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.menu.domain.CreateDraftMenuUseCase
import org.shareat.feature.menu.domain.CreateDraftMenuUseCaseImpl
import org.shareat.feature.menu.domain.LoadOwnedRestaurantMenuUseCase
import org.shareat.feature.menu.domain.LoadOwnedRestaurantMenuUseCaseImpl
import org.shareat.feature.menu.ui.MenuManagementViewModel

val menuUiModule: Module = module {
    factory<LoadOwnedRestaurantMenuUseCase> { LoadOwnedRestaurantMenuUseCaseImpl(get(), get(), get()) }
    factory<CreateDraftMenuUseCase> { CreateDraftMenuUseCaseImpl(get(), get(), get()) }
    viewModel { MenuManagementViewModel(get(), get()) }
}
