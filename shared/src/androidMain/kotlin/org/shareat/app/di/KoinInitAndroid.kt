package org.shareat.app.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

fun initKoinAndroid(context: Context) {
    initKoin {
        androidContext(context)
        androidLogger()
    }
}