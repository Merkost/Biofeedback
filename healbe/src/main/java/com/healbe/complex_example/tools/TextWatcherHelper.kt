package com.healbe.complex_example.tools

import android.text.Editable
import android.text.TextWatcher

object TextWatcherHelper {
    fun onTextChanged(listener: (String) -> Unit): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener(s?.toString() ?: "")
        }
    }
}