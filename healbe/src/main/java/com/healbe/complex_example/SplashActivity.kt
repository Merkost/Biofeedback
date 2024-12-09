package com.healbe.complex_example

import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.healbe.complex_example.connect.ConnectActivity
import com.healbe.complex_example.databinding.ActivitySplashBinding
import com.healbe.complex_example.enter.EnterActivity
import com.healbe.examples.common.BluetoothBroadcastReceiver
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.user.data.HealbeSessionState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    // for saving any opened subscribers in activity
    private val destroy = CompositeDisposable()

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(LayoutInflater.from(this))
        with (binding) {
            setContentView(root)
            // FIXME:  
//            versionText.text = getString(R.string.sdk_example, BuildConfig.BUILD_TYPE)
        }
        applicationContext.registerReceiver(
            BluetoothBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        BluetoothBroadcastReceiver.init(applicationContext)
        lifecycleScope.launch {
            delay(2000)
            initSdk()
        }
    }

    // init sdk
    private fun initSdk() {
        Timber.d("Initializing SDK")
        // in big project recommended to move sdk operations in some abstract layer like presenters in mvp or something else
        // in example its ok, but remember our "destroy" disposable to support lifecycle
        val configuration = HealbeSdk.Configuration.Builder().setGroupMinutes(1).build()
        destroy.add(
            HealbeSdk.init(applicationContext, configuration)
                .subscribe(
                    // after SDK initialization we need to prepare user session
                    {
                        Timber.d("SDK initialized successfully")
                        initSession()
                    },
                    //and we need to consume error in each call of sdk reactive methods
                    //when we don't know what to do with error - we log it with Timber
                    { t ->
                        Timber.e(t, "Error initializing SDK")
                    }
                )
        )
    }

    // session preparing
    private fun initSession() {
        Timber.d("Preparing session")
        destroy.add(
            HealbeSdk.get().USER.prepareSession()
                // all sdk rx methods work in own schedulers, so we need to listen them in UI thread for UI operations
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { sessionState: HealbeSessionState ->
                        Timber.d("Session prepared successfully: $sessionState")
                        sessionStateSwitch(sessionState)
                    },
                    { t ->
                        Timber.e(t, "Error preparing session")
                    }
                )
        )
    }

    private fun sessionStateSwitch(sessionState: HealbeSessionState) {
        Timber.d("Switching session state: $sessionState")
        when (sessionState) {
            // user profile is valid
            HealbeSessionState.VALID_OLD_USER,
            HealbeSessionState.VALID_NEW_USER -> goConnect()

            // there is no authorized user or some data is missing or invalid
            HealbeSessionState.USER_NOT_AUTHORIZED,
            HealbeSessionState.NEED_TO_FILL_PERSONAL,
            HealbeSessionState.NEED_TO_FILL_PARAMS -> goEnter()

            // SDK isn't ready to work, try to prepare session
            HealbeSessionState.SESSION_NOT_PREPARED -> initSession()
        }
    }

    private fun goEnter() {
        Timber.d("Navigating to EnterActivity")
        EnterActivity.start(this)
        finish()
    }

    private fun goConnect() {
        Timber.d("Navigating to ConnectActivity")
        ConnectActivity.start(this)
        finish()
    }

    override fun onDestroy() {
        // clear all subscribes
        destroy.clear()
        super.onDestroy()
    }
}