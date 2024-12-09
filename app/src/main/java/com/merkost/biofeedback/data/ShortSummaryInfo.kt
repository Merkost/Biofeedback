package com.merkost.biofeedback.data

import com.healbe.complex_example.dashboard.SummaryInfo
import com.healbe.healbesdk.business_api.healthdata.data.heart.CurrentHeartRate
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.NeuroData

data class ShortSummaryInfo(
    val isConnected: Boolean,
    val isOnHand: Boolean,
    val heartRate: CurrentHeartRate,
    val neuroData: NeuroData,
) {
    fun SummaryInfo.isActive() = this@ShortSummaryInfo.isConnected && this.isOnHand
}