package com.healbe.complex_example.connect

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.fragments.ConnectFragment
import com.healbe.complex_example.connect.fragments.EnterPinFragment
import com.healbe.complex_example.connect.fragments.ErrorFragment
import com.healbe.complex_example.connect.fragments.PreparingFragment
import com.healbe.complex_example.connect.fragments.SearchFragment
import com.healbe.complex_example.connect.fragments.SetupPinFragment
import com.healbe.complex_example.dashboard.DashboardActivity
import com.healbe.complex_example.enter.EnterActivity
import com.healbe.complex_example.tools.BaseActivity
import com.healbe.examples.common.BluetoothBroadcastReceiver
import com.healbe.healbesdk.business_api.HealbeSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ConnectActivity : BaseActivity(), ConnectRouter {
    private val destroy = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSdkInitialized)
            return

        // return to preparation if Bluetooth is turned off
        destroy.add(
            BluetoothBroadcastReceiver.observeBluetoothState()
                .filter { state -> state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { goState(ConnectRouter.State.PREPARING, true) },
                    { e -> Timber.e(e)}
                )
        )

        if (savedInstanceState == null) {
            goState(ConnectRouter.State.PREPARING, false)
        }
    }

    override fun goState(state: ConnectRouter.State, back: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment?.tag == state.toString())
            return

        with (supportFragmentManager.beginTransaction()) {
            if (fragment != null)
                setCustomAnimations(
                    if (back) R.anim.enter_from_left else R.anim.enter_from_right,
                    if (back) R.anim.exit_to_right else R.anim.exit_to_left
                )

            // note what we don't need to save history here for back-navigation, it's boring
            // this way we have own predictable routing
            replace(android.R.id.content, state.toFragment(), state.toString())
            commit()
        }
    }

    override fun logout() {
        destroy.add(
            HealbeSdk.get().USER.logout()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { goEnter() },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    private fun goEnter() {
        EnterActivity.start(this)
        finish()
    }

    override fun connected() {
        goDashboard()
    }

    private fun goDashboard() {
        val bundle = ActivityOptionsCompat.makeCustomAnimation(
            this,
            android.R.anim.fade_in, android.R.anim.fade_out
        ).toBundle()
        startActivity(Intent(this, DashboardActivity::class.java), bundle)
        finish()
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    //fragment routing map
    private fun ConnectRouter.State.toFragment(): Fragment = when (this) {
        ConnectRouter.State.PREPARING -> PreparingFragment.newInstance()
        ConnectRouter.State.CONNECT -> ConnectFragment.newInstance()
        ConnectRouter.State.SEARCH -> SearchFragment.newInstance()
        ConnectRouter.State.ERROR -> ErrorFragment.newInstance()
        ConnectRouter.State.ENTER_PIN -> EnterPinFragment.newInstance()
        ConnectRouter.State.SETUP_PIN -> SetupPinFragment.newInstance()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, ConnectActivity::class.java),
                createAnimationBundle(context)
            )
        }

        private fun createAnimationBundle(context: Context) = ActivityOptionsCompat
            .makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
            .toBundle()
    }
}