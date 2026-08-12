package org.shareat.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin() {
    initKoin(config = null)
}

internal fun initKoin(config: KoinAppDeclaration?) {
    startKoin {
        includes(config)
        modules(
            sharedModule,
            platformModule
        )
    }
}
