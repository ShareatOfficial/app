package org.shareat.feature.profile.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCaseImpl
import org.shareat.feature.profile.domain.SignOutUseCase
import org.shareat.feature.profile.domain.SignOutUseCaseImpl
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCaseImpl
import org.shareat.feature.profile.ui.EditProfileViewModel

val profileUiModule: Module = module {
    factory<LoadProfileSettingsUseCase> { LoadProfileSettingsUseCaseImpl(get(), get(), get()) }
    factory<UpdateRestaurantInfoUseCase> { UpdateRestaurantInfoUseCaseImpl(get()) }
    factory<SignOutUseCase> { SignOutUseCaseImpl(get()) }
    viewModel { EditProfileViewModel(get(), get(), get()) }
}
