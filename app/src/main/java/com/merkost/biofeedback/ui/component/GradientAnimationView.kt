package com.merkost.biofeedback.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.TileMode

@Composable
fun GradientAnimationView() {
    val isDarkTheme = isSystemInDarkTheme()

    val (initialColors, targetColors) = if (isDarkTheme) {
        listOf(
            Color(0xFF0D1B2A),
            Color(0xFF415A77),
            Color(0xFF1B263B)
        ) to listOf(
            Color(0xFF778DA9),
            Color(0xFF1B263B),
            Color(0xFF0D1B2A)
        )
    } else {
        listOf(
            Color(0xFF3EADCF),
            Color(0xffabcde9),
            Color(0xFFFCFFFB)
        ) to listOf(
            Color(0xfff6ffe0),
            Color(0xff8eddff),
            Color(0xFF3EADCF)
        )
    }

    val animatedColors = getAnimatedColors(initialColors, targetColors)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = animatedColors,
                tileMode = TileMode.Mirror
            )
        )
    }
}
