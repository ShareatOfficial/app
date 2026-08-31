package org.shareat.feature.menu.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.menu.domain.ArchiveDishUseCase
import org.shareat.feature.menu.domain.ArchiveDishUseCaseImpl
import org.shareat.feature.menu.domain.DeleteDishUseCase
import org.shareat.feature.menu.domain.DeleteDishUseCaseImpl
import org.shareat.feature.menu.domain.DeleteMenuUseCase
import org.shareat.feature.menu.domain.DeleteMenuUseCaseImpl
import org.shareat.feature.menu.domain.LoadMenuManagementUseCase
import org.shareat.feature.menu.domain.LoadMenuManagementUseCaseImpl
import org.shareat.feature.menu.domain.SaveDishUseCase
import org.shareat.feature.menu.domain.SaveDishUseCaseImpl
import org.shareat.feature.menu.domain.SaveRestaurantMenuUseCase
import org.shareat.feature.menu.domain.SaveRestaurantMenuUseCaseImpl
import org.shareat.feature.menu.ui.MenuManagementViewModel
import org.shareat.feature.menu.ui.DishImageProcessor
import org.shareat.feature.menu.ui.FileKitDishImageProcessor

val menuUiModule: Module = module {
    factory<LoadMenuManagementUseCase> { LoadMenuManagementUseCaseImpl(get(), get(), get(), get()) }
    factory<SaveRestaurantMenuUseCase> { SaveRestaurantMenuUseCaseImpl(get()) }
    factory<SaveDishUseCase> { SaveDishUseCaseImpl(get()) }
    factory<ArchiveDishUseCase> { ArchiveDishUseCaseImpl(get()) }
    factory<DeleteDishUseCase> { DeleteDishUseCaseImpl(get()) }
    factory<DeleteMenuUseCase> { DeleteMenuUseCaseImpl(get()) }
    factory<DishImageProcessor> { FileKitDishImageProcessor() }
    viewModel { MenuManagementViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}
