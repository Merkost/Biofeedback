package com.healbe.examples.common

import android.content.Context
import java.util.Locale

fun Context.getCurrentLocale(): Locale =
    resources.configuration.locales.get(0) ?: Locale.getDefault()