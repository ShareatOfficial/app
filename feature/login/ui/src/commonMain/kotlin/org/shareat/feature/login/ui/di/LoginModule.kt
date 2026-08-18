package org.shareat.feature.login.ui.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.feature.login.ui.LoginViewModel

val loginModule: Module = module {
    factory { LoginViewModel(get()) }
}
