package com.healbe.complex_example.tools

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import com.healbe.complex_example.R
import com.healbe.examples.common.getCurrentLocale
import com.healbe.healbesdk.business_api.healthdata.data.neuroactivity.GsrActivityLevel
import com.healbe.healbesdk.business_api.healthdata.data.stress.StressState
import com.healbe.healbesdk.business_api.healthdata.data.water.HydrationState
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.truncate

object UnitsFormatter {
    @JvmStatic
    fun stepsAndTime(context: Context, steps: Int, minutes: Int): String {
        val active = context.getString(R.string.active)
        val stepsUnit = context.resources.getQuantityString(R.plurals.plurals_steps, steps)
        val dfSteps = DecimalFormat().apply {
            groupingSize = 3
            isGroupingUsed = true
        }
        return if (minutes > 0) {
            "%s %s, %s %s".format(
                dfSteps.format(steps.toLong()),
                stepsUnit,
                timePeriodFromMins(context, minutes),
                active
            )
        } else {
            "%s %s %s".format(dfSteps.format(steps.toLong()), stepsUnit, active)
        }
    }

    @JvmStatic
    fun timePeriodFromMins(context: Context, totalLength: Int): String {
        val sHours = context.getString(R.string.time_period_hours)
        val sMins = context.getString(R.string.time_period_mins)
        val mins = totalLength % 60
        val hours = totalLength / 60
        return if (hours != 0) {
            context.getString(R.string.time_period_long, hours, sHours, mins, sMins)
        } else {
            context.getString(R.string.time_period_short, mins, sMins)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun energyBalance(context: Context, value: Int, showZero: Boolean = false): CharSequence {
        if (!showZero && value == 0) {
            return SpannableString(context.getString(R.string.na)).apply {
                setSpan(TypefaceSpan("sans-serif-medium"), 0, length, Spannable.SPAN_POINT_POINT)
            }
        }

        val decimalFormat = DecimalFormat().apply {
            groupingSize = 3
            isGroupingUsed = true
            positivePrefix = "+"
            negativePrefix = "-"
        }
        val vl = decimalFormat.format(value.toLong())
        val kcal = context.getString(R.string.kcal)
        return SpannableString("$vl $kcal").apply {
            setSpan(TypefaceSpan("sans-serif-medium"), 0, vl.length, Spannable.SPAN_POINT_POINT)
        }
    }

    @JvmStatic
    fun formatHydrationState(context: Context, state: HydrationState?): CharSequence =
        SpannableString(state.toDisplayString(context)).apply {
            setSpan(TypefaceSpan("sans-serif-medium"), 0, length, Spannable.SPAN_POINT_POINT)
        }

    @JvmStatic
    fun formatNeuroState(
        context: Context,
        state: GsrActivityLevel?
    ): CharSequence {
        val stateString = state?.toDisplayString(context) ?: context.getString(R.string.calculating)
        return SpannableString(stateString).apply {
            setSpan(
                TypefaceSpan("sans-serif-medium"),
                0,
                stateString.length,
                Spannable.SPAN_POINT_POINT
            )
        }
    }

    @JvmStatic
    fun formatStressState(context: Context, state: StressState, value: Float): CharSequence {
        val stateString = state.toDisplayString(context)
        val builder = StringBuilder().apply {
            append(stateString)
            if (state != StressState.NO_DATA && state != StressState.CALCULATING) {
                append(" (")
                append(formatFloatMaxN(value, 1))
                append(")")
            }
        }
        return SpannableString(builder.toString()).apply {
            setSpan(
                TypefaceSpan("sans-serif-medium"),
                0,
                stateString.length,
                Spannable.SPAN_POINT_POINT
            )
        }
    }

    private fun HydrationState?.toDisplayString(context: Context): String =
        when (this) {
            HydrationState.LOW -> context.getString(R.string.low)
            HydrationState.NORMAL -> context.getString(R.string.normal)
            HydrationState.CALCULATING -> context.getString(R.string.calculating)
            else -> context.getString(R.string.no_data)
        }

    private fun GsrActivityLevel.toDisplayString(context: Context): String =
        context.getString(
            when (this) {
                GsrActivityLevel.CALMNESS -> R.string.neuro_calm
                GsrActivityLevel.EFFICACY -> R.string.neuro_effic
                GsrActivityLevel.TENSION -> R.string.neuro_tension
                GsrActivityLevel.OVERLOAD -> R.string.neuro_overload
            }
        )

    private fun StressState?.toDisplayString(context: Context): String =
        when (this) {
            StressState.VERY_HIGH -> context.getString(R.string.level_very_high)
            StressState.HIGH -> context.getString(R.string.level_high)
            StressState.MODERATE -> context.getString(R.string.level_moderate)
            StressState.LIGHT -> context.getString(R.string.level_light)
            StressState.NO_STRESS -> context.getString(R.string.level_no_stress)
            StressState.CALCULATING -> context.getString(R.string.calculating)
            else -> context.getString(R.string.no_data)
        }

    @Suppress("SameParameterValue")
    private fun formatFloatMaxN(f: Float, precision: Int): String {
        val resFloat = f.withPrecision(precision)
        return if (resFloat == truncate(resFloat)) {
            "%d".format(resFloat.toLong())
        } else {
            "%.${resFloat.getFloatPrecision()}f".format(resFloat)
        }
    }

    private fun Float.withPrecision(precision: Int): Float =
        BigDecimal(toDouble())
            .setScale(precision, RoundingMode.HALF_EVEN)
            .toFloat()

    private fun Float.getFloatPrecision(): Int =
        "%s".format(this)
            .split("\\D".toRegex())
            .dropLastWhile { it.isEmpty() }
            .lastOrNull()
            ?.length
            ?: 0

    @JvmStatic
    fun formatPulseSleepAwake(context: Context, pulseAwake: Int, pulseAsleep: Int): String =
        buildList(2) {
            val locale = context.getCurrentLocale()
            if (pulseAwake > 0) {
                add(
                    "%s %d %s".format(
                        context.getString(R.string.awake).lowercase(locale),
                        pulseAwake,
                        context.getString(R.string.bpm)
                    )
                )
            }

            if (pulseAsleep > 0) {
                add(
                    "%s %d %s".format(
                        context.getString(R.string.asleep).lowercase(locale),
                        pulseAsleep,
                        context.getString(R.string.bpm)
                    )
                )
            }
        }.joinToString()

    @JvmStatic
    fun pulseSpan(context: Context, value: Int): CharSequence {
        val decimalFormat = DecimalFormat().apply {
            groupingSize = 3
            isGroupingUsed = true
        }
        val vl = decimalFormat.format(value.toLong())
        val bpm = context.getString(R.string.bpm)
        return SpannableString("$vl $bpm").apply {
            setSpan(TypefaceSpan("sans-serif-medium"), 0, vl.length, Spannable.SPAN_POINT_POINT)
            setSpan(TypefaceSpan("sans-serif"), vl.length + 1, length, Spannable.SPAN_POINT_POINT)
        }
    }

    @JvmStatic
    fun minutesBoldValNormalHelper(context: Context, value: Int): CharSequence {
        val minutes = value % 60
        val hours = value / 60
        val min = minutes.toString()
        val h = hours.toString()
        val sHours = context.getString(R.string.time_period_hours)
        val sMins = context.getString(R.string.time_period_mins)
        val timeString = timePeriodFromMins(context, value)
        return SpannableString(timeString).apply {
            if (hours == 0) {
                val minStartIndex = timeString.indexOf(min)
                if (minStartIndex >= 0) {
                    setSpan(
                        TypefaceSpan("sans-serif-medium"),
                        minStartIndex,
                        min.length,
                        Spannable.SPAN_POINT_POINT
                    )
                    setSpan(
                        TypefaceSpan("sans-serif"),
                        min.length + 1,
                        length,
                        Spannable.SPAN_POINT_POINT
                    )
                }
            } else {
                val hStart = timeString.indexOf("$h $sHours")
                val mStart = timeString.indexOf("$min $sMins")
                if (hStart >= 0) {
                    setSpan(
                        TypefaceSpan("sans-serif-medium"),
                        hStart,
                        h.length,
                        Spannable.SPAN_POINT_POINT
                    )
                }
                @Suppress("ConvertTwoComparisonsToRangeCheck")
                if (hStart >= 0 && mStart > hStart) {
                    setSpan(
                        TypefaceSpan("sans-serif"),
                        h.length + 1,
                        mStart - 1,
                        Spannable.SPAN_POINT_POINT
                    )
                }
                if (mStart >= 0) {
                    setSpan(
                        TypefaceSpan("sans-serif-medium"),
                        mStart,
                        mStart + min.length,
                        Spannable.SPAN_POINT_POINT
                    )
                    setSpan(
                        TypefaceSpan("sans-serif"),
                        mStart + min.length + 1,
                        length,
                        Spannable.SPAN_POINT_POINT
                    )
                }
            }
        }
    }
}