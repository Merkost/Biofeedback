package com.merkost.biofeedback.data

import android.content.Context
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.device_api.ClientState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import timber.log.Timber

class HealBeRepository(
    val context: Context,
    healbeExceptionHandler: HealbeExceptionHandler
) {

    val exFlow = healbeExceptionHandler.exceptionsFlow

    private lateinit var sdk: HealbeSdk

    private val _isSdkInitialized = MutableStateFlow(::sdk.isInitialized)
    val isSdkInitialized = _isSdkInitialized.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val healBeConnectionState = isSdkInitialized.flatMapLatest {
        if (::sdk.isInitialized) sdk.GOBE.observeConnectionState().asFlow()
        else flowOf(ClientState.DISCONNECTED)
    }

    val isHealBeConnected = healBeConnectionState.map { it == ClientState.CONNECTED }


    suspend fun initSdk() {
        try {
            HealbeSdk.init(context).await()
            sdk = HealbeSdk.get()
            _isSdkInitialized.update { true }
        } catch (e: Throwable) {
            exFlow.tryEmit(AppException(e, "Error initializing SDK"))
            _isSdkInitialized.update { false }
        }
    }

    suspend fun connectToGoBe() {
        try {
            sdk.GOBE.connect().await()
        } catch (t: Throwable) {
            exFlow.tryEmit(AppException(t, "Error connecting to GoBe"))
            Timber.e(t)
        }
    }

}