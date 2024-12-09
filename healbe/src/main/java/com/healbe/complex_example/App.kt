package com.healbe.complex_example

import android.app.Application
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // just timber debug tree here
        // we can also init out sdk in a blocking way, but we won't
        // for sdk we use splash screen and background initializing, not in ui thread
        Timber.plant(Timber.DebugTree())
    }
}