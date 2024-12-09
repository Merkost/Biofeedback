package com.merkost.biofeedback.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healbe.healbesdk.business_api.healthdata.flow.neuro
import com.healbe.healbesdk.device_api.ClientState
import com.merkost.biofeedback.data.HealBeRepository
import com.merkost.biofeedback.data.HeartRateRepository
import com.merkost.biofeedback.data.ShortSummaryInfo
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

    val healbeConnected = healbeRepository.isHealBeReady
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

    private fun collectDebugRates() {
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
                healbeRepository.sdk.HEALTH_DATA.observeHeartRate().asFlow().collectLatest {
                    Timber.tag("HeartRate").d("HEALTH_DATA Heart: ${it}")

                    val currentRates = _heartRates.value.toMutableList()
                    if (currentRates.size >= 20) {
                        currentRates.removeAt(0)
                    }
                    currentRates.add(it.heartRate)
                    _heartRates.update { currentRates }
                }
            }

            launch {
                healbeRepository.sdk.HEALTH_DATA.neuro.observeLastNeuroData().collectLatest {
                    Timber.tag("HeartRate").d("HEALTH_DATA neuro: ${it}")

                    val currentGsrRates = _gsrRates.value.toMutableList()
                    if (currentGsrRates.size >= 20) {
                        currentGsrRates.removeAt(0)
                    }
                    currentGsrRates.add(it.gsrActivity)
                    _gsrRates.update { currentGsrRates }
                }
            }

            combine(
                healbeRepository.sdk.GOBE.observeConnectionState()
                    .map { it == ClientState.READY }
                    .distinctUntilChanged()
                    .asFlow(),
                healbeRepository.sdk.TASKS.observeSensorState()
                    .map { it.isOnHand }
                    .distinctUntilChanged()
                    .asFlow(),
                healbeRepository.sdk.HEALTH_DATA.observeHeartRate().asFlow(),
                healbeRepository.sdk.HEALTH_DATA.neuro.observeLastNeuroData(),
                healbeRepository.sdk.GOBE.observe()
                    .map { device -> device.version }
                    .distinctUntilChanged()
                    .onBackpressureLatest()
                    .toObservable()
                    .asFlow()
            ) { isConnected, onHand, heartRate, neuroData, goBeVersion ->
                Timber.tag("ALL")
                    .d("isConnected: $isConnected, onHand: $onHand, heartRate: $heartRate, neuroData: $neuroData, goBeVersion: $goBeVersion")

                Pair(
                    ShortSummaryInfo(
                        isConnected, onHand,
                        heartRate, neuroData
                    ),
                    goBeVersion
                )
            }.distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { (todaySummaryInfo, goBeVersion) ->
                    Timber.tag("ALL")
                        .d("todaySummaryInfo: $todaySummaryInfo, goBeVersion: $goBeVersion")
                    shortSummaryInfo.update { todaySummaryInfo }
                    gobeVersion.update { goBeVersion.name }
                }
        }
    }

}