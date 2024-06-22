package com.merkost.biofeedback.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.biofeedback.ui.theme.Blue40

@Composable
fun TimerCircleView(
    modifier: Modifier = Modifier,
    totalTime: Int,
    borderColor: Color = Blue40,
    onTimerFinish: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(totalTime) }

    val isDark = isSystemInDarkTheme()
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            timeLeft -= 1
        } else {
            onTimerFinish()
        }
    }


    val sweepAngle by animateFloatAsState(360 * (timeLeft / totalTime.toFloat()))

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {

//                drawArc(
//                    color = if (isDark) Color.Gray else Color.LightGray,
//                    startAngle = 0f,
//                    sweepAngle = 360f,
//                    useCenter = false,
//                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
//                )
            drawArc(
                color = borderColor,
                startAngle = 270f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)
            )

        }
        Text(
            text = "$timeLeft",
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

    }
}
