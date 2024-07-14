package com.merkost.biofeedback.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healbe.complex_example.dashboard.ShortSummaryInfo
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.healthdata.rxjava2.neuro
import com.healbe.healbesdk.device_api.ClientState
import com.merkost.biofeedback.data.HealBeRepository
import com.merkost.biofeedback.data.HeartRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber

class VisualizationViewModel(
    private val healbeRepository: HealBeRepository,
    private val repository: HeartRateRepository
) : ViewModel() {

    val isDebug = false
    val uiState: MutableStateFlow<String?> = MutableStateFlow(null)

    val healbeConnection = healbeRepository.healBeConnectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ClientState.DISCONNECTED)

    val healbeConnected = healbeRepository.isHealBeConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val gobeVersion = MutableStateFlow("")
    val shortSummaryInfo = MutableStateFlow<ShortSummaryInfo?>(null)

    private val _heartRates = MutableStateFlow<List<Int>>(emptyList())
    val heartRates: StateFlow<List<Int>> get() = _heartRates

    private val _gsrRates = MutableStateFlow<List<Int>>(emptyList())
    val gsrRates: StateFlow<List<Int>> get() = _gsrRates

    val heartRateData = _heartRates.map {
        it.lastOrNull() ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val gsrData = _gsrRates.map {
        it.lastOrNull() ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    init {
        Timber.tag("VisualizationViewModel").d("init")
    }

    private fun collectRates() {
        viewModelScope.launch {
            launch {
                repository.heartRateFlow.collect { newRate ->
                    val currentRates = _heartRates.value.toMutableList()
                    if (currentRates.size >= 10) {
                        currentRates.removeAt(0)
                    }
                    currentRates.add(newRate)
                    _heartRates.update { currentRates }
                }
            }

            launch {
                repository.gsrFlow.collect { newGsr ->
                    val currentGsrRates = _gsrRates.value.toMutableList()
                    if (currentGsrRates.size >= 10) {
                        currentGsrRates.removeAt(0)
                    }
                    currentGsrRates.add(newGsr)
                    _gsrRates.update { currentGsrRates }
                }
            }
        }
    }

    fun observeData() {
        viewModelScope.launch {
            launch {
                HealbeSdk.get().HEALTH_DATA.observeHeartRate()
                    .asFlow().collectLatest {
                        val currentRates = _heartRates.value.toMutableList()
                        if (currentRates.size >= 20) {
                            currentRates.removeAt(0)
                        }
                        currentRates.add(it.heartRate)
                        _heartRates.update { currentRates }
                    }
            }

            launch {
                HealbeSdk.get().HEALTH_DATA.neuro.observeLastNeuroData()
                    .asFlow().collectLatest {
                        val currentGsrRates = _gsrRates.value.toMutableList()
                        if (currentGsrRates.size >= 20) {
                            currentGsrRates.removeAt(0)
                        }
                        currentGsrRates.add(it.gsrActivity)
                        _gsrRates.update { currentGsrRates }
                    }
            }

            combine(
                HealbeSdk.get().GOBE.observeConnectionState()
                    .map { it == ClientState.READY }
                    .distinctUntilChanged()
                    .asFlow(),
                HealbeSdk.get().TASKS.observeSensorState()
                    .map { it.isOnHand }
                    .distinctUntilChanged()
                    .asFlow(),
                HealbeSdk.get().HEALTH_DATA.observeHeartRate()
                    .asFlow(),
                HealbeSdk.get().HEALTH_DATA.neuro.observeLastNeuroData()
                    .toObservable()
                    .asFlow(),
                HealbeSdk.get().GOBE.observe()
                    .map { device -> device.version }
                    .distinctUntilChanged()
                    .onBackpressureLatest()
                    .toObservable()
                    .asFlow()
            ) { isConnected, onHand, heartRate, neuroData, goBeVersion ->
                Pair(
                    ShortSummaryInfo(
                        isConnected, onHand,
                        heartRate, neuroData
                    ),
                    goBeVersion
                )
            }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { (todaySummaryInfo, goBeVersion) ->
                    shortSummaryInfo.update { todaySummaryInfo }
                    gobeVersion.update { goBeVersion.name }
                }
        }
    }

    fun getGobeData() {
        TODO("Not yet implemented")
    }

}