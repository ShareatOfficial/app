package org.shareat.feature.profile.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.profile.domain.GetAppLanguageSupportUseCase
import org.shareat.feature.profile.domain.GetAppLanguageSupportUseCaseImpl
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.ObserveAppLanguageUseCase
import org.shareat.feature.profile.domain.ObserveAppLanguageUseCaseImpl
import org.shareat.feature.profile.domain.SelectAppLanguageUseCase
import org.shareat.feature.profile.domain.SelectAppLanguageUseCaseImpl
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCaseImpl
import org.shareat.feature.profile.domain.SignOutUseCase
import org.shareat.feature.profile.domain.SignOutUseCaseImpl
import org.shareat.feature.profile.domain.RequestAccountDeletionUseCase
import org.shareat.feature.profile.domain.RequestAccountDeletionUseCaseImpl
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCaseImpl
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCase
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCaseImpl
import org.shareat.feature.profile.ui.editprofile.EditProfileViewModel
import org.shareat.feature.profile.ui.settings.SettingsViewModel

val profileUiModule: Module = module {

    factory<LoadProfileSettingsUseCase> { LoadProfileSettingsUseCaseImpl(get(), get(), get()) }
    factory<UpdateRestaurantInfoUseCase> { UpdateRestaurantInfoUseCaseImpl(get()) }
    factory<UpdateCustomerProfileUseCase> { UpdateCustomerProfileUseCaseImpl(get()) }
    factory<SignOutUseCase> { SignOutUseCaseImpl(get()) }
    factory<RequestAccountDeletionUseCase> { RequestAccountDeletionUseCaseImpl(get()) }
    factory<ObserveAppLanguageUseCase> { ObserveAppLanguageUseCaseImpl(get()) }
    factory<GetAppLanguageSupportUseCase> { GetAppLanguageSupportUseCaseImpl(get()) }
    factory<SelectAppLanguageUseCase> { SelectAppLanguageUseCaseImpl(get()) }

    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
}
