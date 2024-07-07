package com.healbe.complex_example.dashboard

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.healbe.complex_example.databinding.FragmentDashboardBinding
import com.healbe.examples.common.Bluetooth
import com.healbe.examples.common.BluetoothBroadcastReceiver.observeBluetoothState
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.healthdata.data.DaySummary
import com.healbe.healbesdk.business_api.healthdata.data.heart.CurrentHeartRate
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.NeuroData
import com.healbe.healbesdk.business_api.healthdata.data.stress.StressState
import com.healbe.healbesdk.business_api.healthdata.data.water.HydrationState
import com.healbe.healbesdk.business_api.healthdata.rxjava2.neuro
import com.healbe.healbesdk.business_api.tasks.entity.SensorState
import com.healbe.healbesdk.business_api.tasks.entity.TasksState
import com.healbe.healbesdk.business_api.user_storage.entity.GoBeVersion
import com.healbe.healbesdk.business_api.user_storage.entity.HealbeDevice
import com.healbe.healbesdk.device_api.BLEState
import com.healbe.healbesdk.device_api.ClientState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DashboardFragment : Fragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentDashboardBinding? = null

    // here we use holders for visualize combined data
    private var sumHolder: SummaryViewHolder? = null
    private var senHolder: SensorViewHolder? = null

    // error cache not to blinking error states
    private var bleError = false
    private var wbError = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentDashboardBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            initPadding()
            senHolder = SensorViewHolder(sensor)
            sumHolder = SummaryViewHolder(summary)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeToday()
        observeWbState()
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            sumHolder = null
            senHolder = null
            binding = null
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    private fun FragmentDashboardBinding.initPadding() {
        val contentPaddingTop = content.paddingTop
        val summaryBottomMargin = summary.root.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                content.setPadding(
                    content.paddingLeft, contentPaddingTop + insets.top,
                    content.paddingRight, content.paddingBottom
                )
                summary.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = summaryBottomMargin + insets.bottom
                }
            }

            windowInsets
        }
    }

    //observe data and combine it to summary
    private fun observeToday() {
        destroy.add(
            Observable.combineLatest(
                // observe connections
                HealbeSdk.get().GOBE.observeConnectionState().map { it == ClientState.READY }.distinctUntilChanged(),
                HealbeSdk.get().TASKS.observeSensorState().map { it.isOnHand }.distinctUntilChanged(),
                // observe summary data for all modules
                HealbeSdk.get().HEALTH_DATA.getAllDaySummaries(0)
                    .onBackpressureLatest().toObservable(),
                // adding current values to summary
                HealbeSdk.get().HEALTH_DATA.observeHeartRate(),
                HealbeSdk.get().HEALTH_DATA.currentStressLevel.onBackpressureLatest().toObservable(),
                HealbeSdk.get().HEALTH_DATA.currentStressState.onBackpressureLatest().toObservable(),
                HealbeSdk.get().HEALTH_DATA.hydrationState.onBackpressureLatest().toObservable(),
                HealbeSdk.get().HEALTH_DATA.neuro.observeLastNeuroData().toObservable(),
                HealbeSdk.get().GOBE.observe().map { device -> device.version }
                    .distinctUntilChanged().onBackpressureLatest().toObservable()
            ) { isConnected: Boolean, onHand: Boolean, daySummary: DaySummary,
                heartRate: CurrentHeartRate, stressLevel: Float, stressState: StressState,
                hydrationState: HydrationState, neuroData: NeuroData, goBeVersion: GoBeVersion ->

                Pair(
                    SummaryInfo(
                        isConnected, onHand,
                        daySummary,
                        heartRate, hydrationState, neuroData, stressState, stressLevel
                    ),
                    goBeVersion
                )
            }
                .observeOn(Schedulers.computation())
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { (todaySummaryInfo, goBeVersion) -> sumHolder?.setSummary(todaySummaryInfo, goBeVersion) },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    // observe combined state data for wristband
    private fun observeWbState() {
        destroy.add(
            Observable.combineLatest(
                HealbeSdk.get().GOBE.observeConnectionState().observeOn(Schedulers.computation()),
                HealbeSdk.get().TASKS.observeSensorState().observeOn(Schedulers.computation()),
                HealbeSdk.get().TASKS.observeTasksState().observeOn(Schedulers.computation()),
                HealbeSdk.get().TASKS.observeSyncProgress().observeOn(Schedulers.computation()),
                observeBluetoothState().observeOn(Schedulers.computation()),
                HealbeSdk.get().GOBE.observeBleState().observeOn(Schedulers.computation()),
                HealbeSdk.get().GOBE.observe().onBackpressureLatest().toObservable()
                    .observeOn(Schedulers.computation())
            ) { clientState: ClientState, sensorState: SensorState, tasksState: TasksState, syncProgress: Int, bluetoothState: Int, bleState: BLEState, gobe: HealbeDevice ->
                WristbandStateInfo().also { wbState ->
                    val isBluetoothOn = binding?.root?.context?.let { Bluetooth.isOn(it) }
                    wbState.btState =
                        if (isBluetoothOn == true) WristbandStateInfo.BluetoothState.BT_ON
                        else WristbandStateInfo.BluetoothState.BT_OFF
                    wbState.clientState = clientState
                    when {
                        bluetoothState != BluetoothAdapter.STATE_ON -> {
                            wbState.clientState = ClientState.DISCONNECTED
                            wbState.btState = WristbandStateInfo.BluetoothState.BT_OFF
                        }
                        isConnecting(clientState) -> {
                            wbState.clientState = ClientState.CONNECTING
                            wbState.btState = WristbandStateInfo.BluetoothState.BT_ACTIVE
                        }
                        clientState.isBtActive() -> {
                            wbState.btState = WristbandStateInfo.BluetoothState.BT_ACTIVE
                        }
                        clientState == ClientState.READY -> {
                            wbState.btState = WristbandStateInfo.BluetoothState.BT_ONLINE
                        }
                        else -> {
                            wbState.btState = WristbandStateInfo.BluetoothState.BT_ON
                        }
                    }
                    val name = gobe.name.ifBlank { "HEALBE GoBe" }
                    wbState.wristbandName = name
                    wbState.batteryLevel = sensorState.getBatteryLevel()
                    wbState.isCharging = sensorState.isCharging
                    wbState.isOnHand = sensorState.isOnHand
                    wbState.tasksState = tasksState
                    wbState.syncProgress = syncProgress

                    // ble error shows from it appears to next connect
                    if (bleState.shouldShowBleError()) {
                        bleError = true
                        wbState.isBleError = true
                    } else if (clientState == ClientState.CONNECTED) {
                        wbState.isBleError = false
                        bleError = false
                    } else {
                        wbState.isBleError = bleError
                    }

                    // not found error shows from it appears to next connect with ble error priority
                    wbError = if (!wbError) {
                        !wbState.isBleError && clientState == ClientState.CONNECTING && bleState.isWristbandError()
                    } else {
                        !wbState.isBleError && clientState == ClientState.CONNECTING
                    }
                    wbState.isConnectionError = wbError
                }
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { wristbandStateInfo -> senHolder?.setWristbandStateInfo(wristbandStateInfo) },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    private fun isConnecting(clientState: ClientState) =
        (clientState == ClientState.DISCONNECTED || clientState == ClientState.DISCONNECTING) &&
                HealbeSdk.get().GOBE.isConnectionStarted

    private fun BLEState.shouldShowBleError() =
        status == BLEState.Status.INTERNAL_GATT_ERROR || status == BLEState.Status.DISCOVERY_TIMEOUT

    private fun BLEState.isWristbandError() =
        status == BLEState.Status.NOT_FOUND || status == BLEState.Status.OUT_OF_RANGE

    // all state we see when wristband connecting
    private fun ClientState.isBtActive(): Boolean =
        this == ClientState.CONNECTING || this == ClientState.CONNECTED ||
                this == ClientState.AUTHORIZING || this == ClientState.AUTHORIZED ||
                this == ClientState.FW_CHECKING || this == ClientState.FW_CHECKED ||
                this == ClientState.CONFIGURING || this == ClientState.CONFIGURED ||
                this == ClientState.INITIALIZING || this == ClientState.INITIALIZED ||
                this == ClientState.REQUEST_INITIALIZING ||
                this == ClientState.REQUEST_RESET_SENSOR ||
                this == ClientState.REQUEST_SENSOR_START || this == ClientState.WAIT_FOR_RESTART ||
                this == ClientState.USER_INFO_VALIDATING ||
                this == ClientState.USER_INFO_VALIDATED ||
                this == ClientState.USER_CONFIG_VALIDATING ||
                this == ClientState.USER_CONFIG_VALIDATED

    companion object {
        fun newInstance(): DashboardFragment = DashboardFragment()
    }
}