package com.merkost.biofeedback

import android.app.Application
import com.merkost.biofeedback.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BiofeedbackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BiofeedbackApp)
            modules(appModule)
        }
    }
}