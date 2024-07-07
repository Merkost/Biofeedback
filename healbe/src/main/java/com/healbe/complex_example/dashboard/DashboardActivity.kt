package com.healbe.complex_example.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.ConnectActivity
import com.healbe.complex_example.enter.EnterActivity
import com.healbe.complex_example.tools.BaseActivity
import com.healbe.healbesdk.business_api.HealbeSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class DashboardActivity : BaseActivity() {
    private val destroy = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSdkInitialized)
            return

        supportActionBar?.setTitle(R.string.dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            // only dashboard for now
            supportFragmentManager.beginTransaction()
                .apply { replace(android.R.id.content, DashboardFragment.newInstance()) }
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val connectionActive = HealbeSdk.get().GOBE.isConnectionStarted
        menu.findItem(R.id.connect)
            ?.setTitle(if (connectionActive) R.string.disconnect else R.string.connect)
        menu.findItem(R.id.find)?.isVisible = !connectionActive
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }

        R.id.logout -> {
            logout()
            true
        }

        R.id.connect -> {
            connectOrDisconnect()
            true
        }

        R.id.find -> {
            findAnother()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    private fun logout() {
        destroy.add(
            HealbeSdk.get().USER.logout() // logout disconnecting wb itself
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { goEnter() },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    private fun connectOrDisconnect() {
        if (HealbeSdk.get().GOBE.isConnectionStarted) {
            destroy.add(
                HealbeSdk.get().GOBE.disconnect()
                    .andThen(HealbeSdk.get().GOBE.setActive(false)) // for next time connect will start from search
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { invalidateOptionsMenu() }, // stay here, just update menu
                        { t: Throwable -> Timber.e(t) }
                    )
            )
        } else {
            destroy.add(
                HealbeSdk.get().GOBE.connect()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { invalidateOptionsMenu() },
                        { t: Throwable -> Timber.e(t) }
                    )
            )
        }
    }

    private fun findAnother() {
        destroy.add(
            HealbeSdk.get().GOBE.disconnect()
                .andThen(HealbeSdk.get().GOBE.setActive(false)) // for next time connect will start from search
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { goConnect() }, // go search
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    private fun goEnter() {
        EnterActivity.start(this)
        finish()
    }

    private fun goConnect() {
        ConnectActivity.start(this)
        finish()
    }
}