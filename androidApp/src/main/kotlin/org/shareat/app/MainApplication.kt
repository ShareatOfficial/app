package org.shareat.app

import android.app.Application
import org.shareat.app.di.initKoinAndroid

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this, useFakeData = false) // initKoinAndroid(this, useFakeData = true)
    }
}