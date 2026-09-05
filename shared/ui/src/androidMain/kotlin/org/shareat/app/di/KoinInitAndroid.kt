package org.shareat.app.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

fun initKoinAndroid(context: Context, useFakeData: Boolean = false) {
    initKoin(
        config = {
            androidContext(context)
            androidLogger()
        },
        useFakeData = useFakeData,
    )
}
