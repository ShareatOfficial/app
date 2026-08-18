package org.shareat.feature.login.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.feature.login.LoginViewModel

val loginModule: Module = module {
    factory { LoginViewModel(get()) }
}
