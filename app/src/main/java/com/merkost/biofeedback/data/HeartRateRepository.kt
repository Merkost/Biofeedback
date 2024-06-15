package com.merkost.biofeedback.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HeartRateRepository {
    val heartRateFlow: Flow<Int> = flow {
        while (true) {
            emit((60..100).random())
            delay(5000)
        }
    }
}