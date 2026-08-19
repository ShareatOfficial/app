package org.shareat.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(useFakeData: Boolean = false) {
    initKoin(config = null, useFakeData = useFakeData)
}

internal fun initKoin(config: KoinAppDeclaration?, useFakeData: Boolean = false) {
    startKoin {
        includes(config)
        modules(
            if (useFakeData) previewSharedModule else sharedModule,
            platformModule
        )
    }
}
