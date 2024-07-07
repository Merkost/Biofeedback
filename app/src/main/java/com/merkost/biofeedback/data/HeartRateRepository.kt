package com.merkost.biofeedback.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.minutes

class HeartRateRepository {
    val heartRateFlow: Flow<Int> = flow {
        while (true) {
            emit((60..100).random())
            delay(5000)
        }
    }

    val gsrFlow: Flow<Int> = flow {
        while (true) {
            emit((500..2000).random())
            delay(1.minutes)
        }
    }
}