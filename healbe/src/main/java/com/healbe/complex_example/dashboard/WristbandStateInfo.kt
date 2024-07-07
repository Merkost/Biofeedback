package com.healbe.complex_example.dashboard

import com.healbe.healbesdk.business_api.tasks.entity.TasksState
import com.healbe.healbesdk.device_api.ClientState

class WristbandStateInfo {
    var isOnHand = true
    var clientState: ClientState = ClientState.DISCONNECTED
    var wristbandName: String = ""
    var batteryLevel: Int = -1
    var isCharging: Boolean = false
    var btState: BluetoothState = BluetoothState.BT_OFF
    var syncProgress: Int = -1
    var tasksState: TasksState = TasksState.STOPPED
    var isBleError: Boolean = false
    var isConnectionError: Boolean = false

    override fun toString(): String =
        "WristbandStateInfo{" +
                "onHand=$isOnHand, " +
                "clientState=$clientState, " +
                "wristbandName='$wristbandName', " +
                "batteryLevel=$batteryLevel, " +
                "btState=$btState, " +
                "syncProgress=$syncProgress, " +
                "tasksState=$tasksState, " +
                "bleError=$isBleError, " +
                "connectionError=$isConnectionError" +
                "}"

    enum class BluetoothState {
        BT_OFF, BT_ON, BT_ACTIVE, BT_ONLINE
    }
}