package com.healbe.complex_example.dashboard

import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.healbe.complex_example.R
import com.healbe.complex_example.databinding.SummaryLayoutBinding
import com.healbe.complex_example.tools.UnitsFormatter.energyBalance
import com.healbe.complex_example.tools.UnitsFormatter.formatHydrationState
import com.healbe.complex_example.tools.UnitsFormatter.formatNeuroState
import com.healbe.complex_example.tools.UnitsFormatter.formatPulseSleepAwake
import com.healbe.complex_example.tools.UnitsFormatter.formatStressState
import com.healbe.complex_example.tools.UnitsFormatter.minutesBoldValNormalHelper
import com.healbe.complex_example.tools.UnitsFormatter.pulseSpan
import com.healbe.complex_example.tools.UnitsFormatter.stepsAndTime
import com.healbe.healbesdk.business_api.healthdata.data.energy.EnergySummary
import com.healbe.healbesdk.business_api.healthdata.data.heart.AverageType
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.NeuroData
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.NeuroTrend
import com.healbe.healbesdk.business_api.healthdata.data.stress.StressState
import com.healbe.healbesdk.business_api.healthdata.data.water.HydrationState
import com.healbe.healbesdk.business_api.user_storage.entity.GoBeVersion
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SummaryViewHolder(private val binding: SummaryLayoutBinding) {
    private val lastDataColor = ContextCompat.getColor(binding.root.context, R.color.light_black_38)
    private val energyColor = ContextCompat.getColor(binding.root.context, R.color.secondary_purple)
    private val heartColor = ContextCompat.getColor(binding.root.context, R.color.main_heart_red)
    private val waterColor = ContextCompat.getColor(binding.root.context, R.color.main_water_blue)
    private val neuroColor = ContextCompat.getColor(binding.root.context, R.color.neuro_orange)
    private val stressColor =
        ContextCompat.getColor(binding.root.context, R.color.secondary_stress_orange)
    private val sleepColor = ContextCompat.getColor(binding.root.context, R.color.main_sleep_blue)

    fun setSummary(todaySummaryInfo: SummaryInfo, goBeVersion: GoBeVersion) {
        with (binding) {
            setEnergySum(todaySummaryInfo)
            setHeartSum(todaySummaryInfo)
            setHydrationSum(todaySummaryInfo)
            setNeuroSum(todaySummaryInfo, goBeVersion)
            setStressSum(todaySummaryInfo)
            setSleepSum(todaySummaryInfo)
            setIconBackgroundColors(todaySummaryInfo.isActive())
        }
    }

    private fun SummaryInfo.isActive() = isConnected && isOnHand

    private fun SummaryLayoutBinding.setEnergySum(summaryInfo: SummaryInfo) {
        fun EnergySummary.toActivitiesDuration(): Int {
            var result = 0
            if (routineKcal > 0) result += routineMins
            if (walkingKcal > 0) result += walkingMins
            if (runningKcal > 0) result += runningMins
            return result
        }

        summaryInfo.daySummary.energySummary.get()?.let {
            val activitiesDuration = it.toActivitiesDuration()
            energyBar.setText(stepsAndTime(root.context, it.steps, activitiesDuration))
            energyBar.setValueText(
                energyBalance(
                    root.context,
                    it.energyIn.roundToInt() - it.energyOut.roundToInt(),
                    true
                )
            )
            energyBar.setTextVisible(activitiesDuration + it.steps > 0)
            energyBar.setValueVisible(it.energyOut > 0)
        } ?: summaryInfo.run {
            energyBar.setTextVisible(false)
            energyBar.setValueVisible(false)
        }
    }

    private fun SummaryLayoutBinding.setHeartSum(summaryInfo: SummaryInfo) {
        heartBar.setTitleText(
            if (summaryInfo.isActive() && summaryInfo.heartRate.isValid) R.string.pulse_header_cur else R.string.pulse_header
        )

        val pulseStatPresent = summaryInfo.daySummary.heartSummary.get()?.averages?.let { averages ->
            val asleepAverage = averages[AverageType.SLEEP]?.average ?: 0
            val awakeAverage = averages[AverageType.AWAKE]?.average ?: 0
            heartBar.setText(formatPulseSleepAwake(root.context, awakeAverage, asleepAverage))
            asleepAverage + awakeAverage > 0
        } ?: false
        heartBar.setTextVisible(pulseStatPresent)

        if (summaryInfo.heartRate.isValid) {
            heartBar.setValueText(pulseSpan(root.context, summaryInfo.heartRate.heartRate))
            heartBar.setValueVisible(true)
        } else {
            heartBar.setValueVisible(true)
        }
    }

    private fun SummaryLayoutBinding.setHydrationSum(summaryInfo: SummaryInfo) {
        val waterCurPresent = summaryInfo.hydrationState != HydrationState.NO_DATA

        waterBar.setTitleText(if (waterCurPresent) R.string.hydration_header_cur else R.string.hydration_header)

        if (waterCurPresent) {
            waterBar.setValueText(formatHydrationState(root.context, summaryInfo.hydrationState))
            waterBar.setValueVisible(true)
        } else {
            waterBar.setValueVisible(false)
        }

        if (summaryInfo.hydrationState == HydrationState.CALCULATING) {
            waterBar.setText(root.context.getString(R.string.may_take_up_to_hour))
            waterBar.setTextVisible(true)
        } else {
            waterBar.setTextVisible(false)
        }
    }

    private fun SummaryLayoutBinding.setNeuroSum(summaryInfo: SummaryInfo, goBeVersion: GoBeVersion) {
        if (goBeVersion < GoBeVersion.GOBE_3_0) {
            neuroBar.isGone = true
        } else {
            val neuroData = summaryInfo.neuroData

            if (!NeuroData.isEmpty(neuroData)) {
                val state = with (neuroData) {
                    if (calculating) null else neuroData.getCurrentGsrLevel()
                }
                neuroBar.setValueText(formatNeuroState(root.context, state))
                neuroBar.setValueVisible(true)
            } else {
                neuroBar.setValueVisible(false)
            }

            if (neuroData.trend != NeuroTrend.STABLE) {
                val trend = root.context.getString(
                    if (neuroData.trend == NeuroTrend.GOES_UP) R.string.neuro_trend_up
                    else R.string.neuro_trend_down
                )
                stressBar.setText(trend)
                stressBar.setTextVisible(true)
            } else {
                stressBar.setTextVisible(false)
            }
        }
    }

    private fun SummaryLayoutBinding.setStressSum(summaryInfo: SummaryInfo) {
        stressBar.setTitleText(
            if (summaryInfo.isActive()) R.string.stress_header_cur else R.string.stress_header
        )

        if (summaryInfo.stressState != StressState.NO_DATA) {
            stressBar.setValueText(
                formatStressState(root.context, summaryInfo.stressState, summaryInfo.stressLevel)
            )
            stressBar.setValueVisible(true)
        } else {
            stressBar.setValueVisible(false)
        }

        if (summaryInfo.stressState == StressState.CALCULATING) {
            stressBar.setText(root.context.getString(R.string.may_take_up_to_hour))
            stressBar.setTextVisible(true)
        } else {
            stressBar.setTextVisible(false)
        }
    }

    private fun SummaryLayoutBinding.setSleepSum(summaryInfo: SummaryInfo) {
        sleepBar.setTitleText(R.string.sleep_header)

        val sleepSummary = summaryInfo.daySummary.sleepSummary.get()
        if (sleepSummary == null) {
            sleepBar.setTextVisible(false)
            sleepBar.setValueVisible(false)
            return
        }

        val sleepDuration = sleepSummary.getSleepDuration(TimeUnit.MINUTES).toInt()
        if (sleepDuration > 0) {
            sleepBar.setValueText(minutesBoldValNormalHelper(root.context, sleepDuration))
            sleepBar.setValueVisible(true)
        } else {
            sleepBar.setValueVisible(false)
        }

        if (sleepSummary.quality > 0) {
            sleepBar.setText(root.context.getString(R.string.sleep_quality, sleepSummary.quality))
            sleepBar.setTextVisible(true)
        } else {
            sleepBar.setTextVisible(false)
        }
    }

    private fun SummaryLayoutBinding.setIconBackgroundColors(isActive: Boolean) {
        if (isActive) {
            energyBar.setIconBackgroundColor(energyColor)
            heartBar.setIconBackgroundColor(heartColor)
            waterBar.setIconBackgroundColor(waterColor)
            neuroBar.setIconBackgroundColor(neuroColor)
            stressBar.setIconBackgroundColor(stressColor)
            sleepBar.setIconBackgroundColor(sleepColor)
        } else {
            energyBar.setIconBackgroundColor(lastDataColor)
            heartBar.setIconBackgroundColor(lastDataColor)
            waterBar.setIconBackgroundColor(lastDataColor)
            neuroBar.setIconBackgroundColor(lastDataColor)
            stressBar.setIconBackgroundColor(lastDataColor)
            sleepBar.setIconBackgroundColor(lastDataColor)
        }
    }
}