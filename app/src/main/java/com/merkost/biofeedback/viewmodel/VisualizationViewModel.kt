package com.merkost.biofeedback.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.biofeedback.data.HeartRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisualizationViewModel(private val repository: HeartRateRepository) : ViewModel() {
    val heartRateData: StateFlow<Int> = repository.heartRateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 70
    )

    val gsrData: StateFlow<Int> = repository.gsrFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 1000
    )

    private val _heartRates = MutableStateFlow<List<Int>>(emptyList())
    val heartRates: StateFlow<List<Int>> get() = _heartRates

    private val _gsrRates = MutableStateFlow<List<Int>>(emptyList())
    val gsrRates: StateFlow<List<Int>> get() = _gsrRates

    init {
        collectRates()
    }

    private fun collectRates() {
        viewModelScope.launch {
            launch {
                heartRateData.collect { newRate ->
                    val currentRates = _heartRates.value.toMutableList()
                    if (currentRates.size >= 20) {
                        currentRates.removeAt(0)
                    }
                    currentRates.add(newRate)
                    _heartRates.update { currentRates }
                }
            }

            launch {
                gsrData.collect { newGsr ->
                    val currentGsrRates = _gsrRates.value.toMutableList()
                    if (currentGsrRates.size >= 20) {
                        currentGsrRates.removeAt(0)
                    }
                    currentGsrRates.add(newGsr)
                    _gsrRates.update { currentGsrRates }
                }
            }
        }
    }

}