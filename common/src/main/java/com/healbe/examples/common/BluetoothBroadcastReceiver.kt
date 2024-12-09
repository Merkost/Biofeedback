package com.healbe.examples.common

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object BluetoothBroadcastReceiver : BroadcastReceiver() {
    private val subj = BehaviorSubject.createDefault(BluetoothAdapter.STATE_OFF)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action, ignoreCase = true))
            subj.onNext(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1))
    }

    fun init(context: Context) {
        subj.onNext(
            if (Bluetooth.isOn(context)) BluetoothAdapter.STATE_ON
            else BluetoothAdapter.STATE_OFF
        )
    }

    @JvmStatic
    fun observeBluetoothState(): Observable<Int> = subj
}