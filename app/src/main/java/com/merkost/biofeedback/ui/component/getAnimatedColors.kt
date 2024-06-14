package com.merkost.biofeedback.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun getAnimatedColors(
    initialColors: List<Color>,
    targetColors: List<Color>,
    durationMillis: Int = 5000
): List<Color> {
    val infiniteTransition = rememberInfiniteTransition()

    return initialColors.indices.map { index ->
        infiniteTransition.animateColor(
            initialValue = initialColors[index],
            targetValue = targetColors[index],
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    }
}
