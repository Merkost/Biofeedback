package com.merkost.biofeedback.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.merkost.biofeedback.R
import com.merkost.biofeedback.ui.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DoubleBackPressHandler(enabled: Boolean = true) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBackPressed = remember { mutableStateOf(false) }
    BackHandler(enabled && !isBackPressed.value) {
        isBackPressed.value = true
        context.showToast("Press back again to exit")
        scope.launch {
            delay(2000L)
            isBackPressed.value = false
        }
    }
}