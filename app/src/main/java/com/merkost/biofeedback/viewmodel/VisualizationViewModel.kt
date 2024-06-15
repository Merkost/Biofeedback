package com.merkost.biofeedback.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.biofeedback.data.HeartRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VisualizationViewModel(private val repository: HeartRateRepository) : ViewModel() {
    val heartRateData: StateFlow<Int> = repository.heartRateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 70
    )

    private val _heartRates = MutableStateFlow<List<Int>>(emptyList())
    val heartRates: StateFlow<List<Int>> get() = _heartRates

    init {
        collectHeartRates()
    }

    private fun collectHeartRates() {
        viewModelScope.launch {
            repository.heartRateFlow.collect { newRate ->
                val currentRates = _heartRates.value.toMutableList()
                if (currentRates.size >= 20) {
                    currentRates.removeAt(0)
                }
                currentRates.add(newRate)
                _heartRates.value = currentRates
            }
        }
    }

}