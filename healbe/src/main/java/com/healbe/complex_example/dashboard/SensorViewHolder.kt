package com.healbe.complex_example.dashboard

import android.view.View
import androidx.core.content.ContextCompat
import com.healbe.complex_example.R
import com.healbe.complex_example.dashboard.WristbandStateInfo.BluetoothState
import com.healbe.complex_example.databinding.SensorLayoutBinding
import com.healbe.healbesdk.business_api.tasks.entity.TasksState
import com.healbe.healbesdk.device_api.ClientState

class SensorViewHolder(private val binding: SensorLayoutBinding) {
    private val mainPurpleColor =
        ContextCompat.getColor(binding.root.context, com.healbe.examples.common.R.color.main_purple)
    private val lightBlackTextColor =
        ContextCompat.getColor(binding.root.context, R.color.light_black_54)
    private val lightBlackIconColor =
        ContextCompat.getColor(binding.root.context, R.color.light_black_38)
    private val validationRedColor =
        ContextCompat.getColor(binding.root.context, R.color.validation_red)

    fun setWristbandStateInfo(stateInfo: WristbandStateInfo) {
        binding.apply {
            stateInfo.setWristbandName()
            setConnectionState(stateInfo)
            setBluetoothState(stateInfo)
            setSyncProgress(stateInfo)
            setChargingValue(stateInfo)
            setBottomView(stateInfo)
        }
    }

    private fun WristbandStateInfo.setWristbandName() {
        binding.title.text = wristbandName
    }

    private fun SensorLayoutBinding.setConnectionState(wristbandStateInfo: WristbandStateInfo) {
        progress.visibility = View.GONE
        when (DeviceState.fromStates(wristbandStateInfo)) {
            DeviceState.CONNECTED, DeviceState.SYNCING -> {
                round.setCardBackgroundColor(mainPurpleColor)
                indicator.visibility = View.VISIBLE
                if (wristbandStateInfo.tasksState != TasksState.SYNC) status.setText(R.string.connected)
                status.setTextColor(lightBlackTextColor)
            }

            DeviceState.CONNECTING -> {
                status.setText(R.string.connecting)
                status.setTextColor(lightBlackTextColor)
                round.setCardBackgroundColor(lightBlackIconColor)
                indicator.visibility = View.GONE
            }

            DeviceState.DISCONNECTED -> {
                round.setCardBackgroundColor(lightBlackIconColor)
                status.setTextColor(lightBlackTextColor)
                status.setText(R.string.disconnected)
                indicator.visibility = View.GONE
            }
        }
    }

    private fun SensorLayoutBinding.setSyncProgress(wristbandStateInfo: WristbandStateInfo) {
        if (wristbandStateInfo.tasksState == TasksState.SYNC && wristbandStateInfo.clientState == ClientState.READY) {
            val progressPercent = wristbandStateInfo.syncProgress
            status.text = root.context.getString(R.string.synchronization_p, progressPercent)
            progress.progress = progressPercent
            if (progress.visibility != View.VISIBLE) progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
    }

    private fun SensorLayoutBinding.setBluetoothState(wristbandStateInfo: WristbandStateInfo) {
        bluetooth.setImageResource(
            when (wristbandStateInfo.btState) {
                BluetoothState.BT_ACTIVE -> R.drawable.ic_bluetooth_search
                BluetoothState.BT_ON -> R.drawable.ic_bluetooth
                BluetoothState.BT_OFF -> R.drawable.ic_bluetooth_not_connected
                else -> R.drawable.ic_bluetooth_connected_black
            }
        )
    }

    private fun SensorLayoutBinding.setChargingValue(wristbandStateInfo: WristbandStateInfo) {
        val percents = wristbandStateInfo.batteryLevel
        indicator.visibility =
            if (percents in 0..100 && wristbandStateInfo.clientState == ClientState.READY) View.VISIBLE
            else View.GONE
        indicator.text =
            if (wristbandStateInfo.isCharging) root.context.getString(R.string.charging)
            else root.context.getString(R.string.percents, percents)
    }

    private fun SensorLayoutBinding.setBottomView(wristbandStateInfo: WristbandStateInfo) {
        if (wristbandStateInfo.btState == BluetoothState.BT_OFF) {
            //content
            warnText.setText(R.string.enable_bluetooth)
            warnText.setTextColor(validationRedColor)
            badContact.setImageResource(R.drawable.ic_cant_find)
            //visibility
            warnText.visibility = View.VISIBLE
            badContact.visibility = View.GONE
            warnCont.visibility = View.VISIBLE
            div.visibility = View.VISIBLE
        } else if (wristbandStateInfo.isBleError) {
            warnText.setText(R.string.bluetooth_error)
            warnText.setTextColor(validationRedColor)
            badContact.setImageResource(R.drawable.ic_cant_find)
            //visibility
            warnText.visibility = View.VISIBLE
            badContact.visibility = View.VISIBLE
            warnCont.visibility = View.VISIBLE
            div.visibility = View.VISIBLE
        } else if (wristbandStateInfo.isConnectionError) {
            warnText.setText(R.string.gobe_connection_error_short)
            warnText.setTextColor(validationRedColor)
            badContact.setImageResource(R.drawable.ic_cant_find)
            //visibility
            warnText.visibility = View.VISIBLE
            badContact.visibility = View.VISIBLE
            warnCont.visibility = View.VISIBLE
            div.visibility = View.VISIBLE
        } else if (wristbandStateInfo.isNoContact()) {
            //content
            warnText.setText(R.string.no_contact)
            warnText.setTextColor(lightBlackTextColor)
            badContact.setImageResource(R.drawable.ic_bad_contact)
            warnText.visibility = View.VISIBLE
            badContact.visibility = View.VISIBLE
            warnCont.visibility = View.VISIBLE
            div.visibility = View.VISIBLE
        } else {
            //visibility
            warnText.visibility = View.GONE
            badContact.visibility = View.GONE
            warnCont.visibility = View.GONE
            div.visibility = View.GONE
        }
    }

    private fun WristbandStateInfo.isNoContact() =
        !isOnHand && clientState == ClientState.READY && !isCharging

    enum class DeviceState {
        DISCONNECTED, CONNECTING, CONNECTED, SYNCING;

        companion object {
            fun fromStates(stateInfo: WristbandStateInfo): DeviceState =
                if (stateInfo.clientState == ClientState.READY) {
                    if (stateInfo.tasksState == TasksState.SYNC) SYNCING else CONNECTED
                } else {
                    if (stateInfo.btState == BluetoothState.BT_ACTIVE) CONNECTING else DISCONNECTED
                }
        }
    }
}