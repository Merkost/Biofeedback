package com.healbe.complex_example.dashboard

import com.healbe.healbesdk.business_api.healthdata.data.DaySummary
import com.healbe.healbesdk.business_api.healthdata.data.heart.CurrentHeartRate
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.NeuroData
import com.healbe.healbesdk.business_api.healthdata.data.stress.StressState
import com.healbe.healbesdk.business_api.healthdata.data.water.HydrationState

data class SummaryInfo(
    val isConnected: Boolean,
    val isOnHand: Boolean,
    val daySummary: DaySummary,
    val heartRate: CurrentHeartRate,
    val hydrationState: HydrationState,
    val neuroData: NeuroData,
    val stressState: StressState,
    val stressLevel: Float
)

data class ShortSummaryInfo(
    val isConnected: Boolean,
    val isOnHand: Boolean,
    val heartRate: CurrentHeartRate,
    val neuroData: NeuroData,
) {
    fun SummaryInfo.isActive() = this@ShortSummaryInfo.isConnected && this.isOnHand
}