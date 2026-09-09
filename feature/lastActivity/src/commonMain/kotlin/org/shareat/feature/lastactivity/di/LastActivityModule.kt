package org.shareat.feature.lastactivity.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.lastactivity.domain.GetLastActivityUseCase
import org.shareat.feature.lastactivity.domain.GetLastActivityUseCaseImpl
import org.shareat.feature.lastactivity.ui.LastActivityViewModel

val lastActivityModule: Module = module {
    factory<GetLastActivityUseCase> { GetLastActivityUseCaseImpl(get(), get(), get()) }
    viewModel { LastActivityViewModel(get(), get()) }
}
