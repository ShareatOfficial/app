package org.shareat.app.di

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(useFakeData: Boolean = false) {
    initKoin(config = null, useFakeData = useFakeData)
}

internal fun initKoin(config: KoinAppDeclaration?, useFakeData: Boolean = false) {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    startKoin {
        includes(config)
        modules(
            if (useFakeData) previewSharedModule else sharedModule,
            platformModule
        )
    }
}
