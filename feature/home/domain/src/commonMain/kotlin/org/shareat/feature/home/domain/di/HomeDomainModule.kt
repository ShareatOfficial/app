package org.shareat.feature.home.domain.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.domain.usecase.di.sharedDomainModule

val homeDomainModule: Module = module {
    includes(sharedDomainModule)
}
