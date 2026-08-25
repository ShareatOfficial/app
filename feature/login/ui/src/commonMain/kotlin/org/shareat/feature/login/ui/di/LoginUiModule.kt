package org.shareat.feature.login.ui.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.login.ui.LoginViewModel

val loginUiModule: Module = module {
    viewModel { LoginViewModel(get()) }
}
