package com.merkost.biofeedback.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

object HealbeExceptionHandler {
    val exceptionsFlow: MutableSharedFlow<AppException?> = MutableStateFlow(null)
}

data class AppException(val exception: Throwable, val message: String)
