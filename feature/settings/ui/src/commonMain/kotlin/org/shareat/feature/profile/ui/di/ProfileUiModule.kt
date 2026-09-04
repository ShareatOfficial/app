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
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCase
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCaseImpl
import org.shareat.feature.profile.domain.CreateRestaurantProfileUseCase
import org.shareat.feature.profile.domain.CreateRestaurantProfileUseCaseImpl
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingViewModel
import org.shareat.feature.profile.ui.editprofile.EditProfileViewModel
import org.shareat.feature.profile.ui.settings.SettingsViewModel

val profileUiModule: Module = module {

    factory<LoadProfileSettingsUseCase> { LoadProfileSettingsUseCaseImpl(get(), get(), get()) }
    factory<UpdateRestaurantInfoUseCase> { UpdateRestaurantInfoUseCaseImpl(get()) }
    factory<UpdateCustomerProfileUseCase> { UpdateCustomerProfileUseCaseImpl(get()) }
    factory<SignOutUseCase> { SignOutUseCaseImpl(get()) }
    factory<CreateRestaurantProfileUseCase> { CreateRestaurantProfileUseCaseImpl(get(), get(), get()) }

    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { RestaurantOnboardingViewModel(get(), get()) }
}
