package com.healbe.complex_example.tools

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.healbe.complex_example.SplashActivity
import com.healbe.healbesdk.business_api.HealbeSdk
import timber.log.Timber

// when we get fatal exception we need to re-init our sdk, so go to splash if sdk not initialized
// we can init it with Application.onCreate instead, but we want more sensitive app
// with not "skipped XXX frames" warning in log, because sdk has long initializing process
abstract class BaseActivity : AppCompatActivity() {
    // because finish() call doesn't prevent the rest of onCreate() code to run in the children,
    // they can use this variable to check should they proceed with initialization or not
    protected var isSdkInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate %s", this.javaClass.simpleName)
        try {
            HealbeSdk.get()
            isSdkInitialized = true
        } catch (e: IllegalStateException) {
            Timber.e(e, "HealbeSdk did not initialized yet!")
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent, null)
            finish()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}