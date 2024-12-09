package com.healbe.complex_example.tools

import android.os.Parcel
import android.os.Parcelable.Creator
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.healbe.healbesdk.business_api.converters.DateConverter
import com.healbe.healbesdk.business_api.tools.ValidatorTool.AGE_BOTTOM
import com.healbe.healbesdk.business_api.tools.ValidatorTool.AGE_DEFAULT
import com.healbe.healbesdk.business_api.tools.ValidatorTool.AGE_TOP
import com.healbe.healbesdk.business_api.tools.ValidatorTool.isValidBirthDate
import com.healbe.healbesdk.business_api.user.data.YMD
import java.util.*

object BirthDateDateValidator : DateValidator {
    override fun isValid(millis: Long): Boolean {
        val date = Date(millis)
        val ymd = DateConverter.dateToYMD(date)
        return isValidBirthDate(ymd)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    @JvmStatic
    val startConstraint: Long by lazy {
        calculateBoundConstraint(AGE_TOP)
    }

    @JvmStatic
    val endConstraint: Long by lazy {
        calculateBoundConstraint(AGE_BOTTOM)
    }

    @JvmStatic
    val defaultOpenAt: Long by lazy {
        calculateBoundConstraint(AGE_DEFAULT)
    }

    private fun calculateBoundConstraint(age: Int): Long =
        with(Calendar.getInstance(TimeZone.getTimeZone("UTC"))) {
            time = DateConverter.ymdToTime(YMD.fromAge(age))
            set(Calendar.DAY_OF_MONTH, 1)
            timeInMillis
        }

    @Suppress("unused")
    @JvmField
    val CREATOR: Creator<BirthDateDateValidator?> = object : Creator<BirthDateDateValidator?> {
        override fun createFromParcel(source: Parcel): BirthDateDateValidator =
            this@BirthDateDateValidator

        override fun newArray(size: Int): Array<BirthDateDateValidator?> = arrayOfNulls(0)
    }
}