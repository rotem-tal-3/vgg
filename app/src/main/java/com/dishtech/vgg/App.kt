package com.dishtech.vgg

import android.app.Application
import com.dishtech.vgg.audio.FFT
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App: Application() {
    private val appModule =  module {
        factory { params -> FFT(params.get(), params.get()) }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@App)
            androidLogger()
            listOf(appModule)
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}