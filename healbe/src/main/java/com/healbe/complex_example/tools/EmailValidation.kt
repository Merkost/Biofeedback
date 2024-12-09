@file:JvmName("EmailValidation")

package com.healbe.complex_example.tools

import androidx.core.util.PatternsCompat

fun CharSequence?.isValidEmailAddress(): Boolean =
    !isNullOrBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()