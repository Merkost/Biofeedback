package com.merkost.biofeedback.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
import com.merkost.biofeedback.ui.theme.PurpleGrey80

@Composable
fun TimerCircleView(
    totalTime: Int,
    onTimerFinish: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(totalTime) }

    val isDark = isSystemInDarkTheme()
    // Timer logic
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            timeLeft -= 1
        } else {
            onTimerFinish()
        }
    }

    Column(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sweepAngle by animateFloatAsState(360 * (timeLeft / totalTime.toFloat()))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {

//                drawArc(
//                    color = if (isDark) Color.Gray else Color.LightGray,
//                    startAngle = 0f,
//                    sweepAngle = 360f,
//                    useCenter = false,
//                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
//                )
                drawArc(
                    color = if (isDark) Color.Gray else Color.White,
                    startAngle = 270f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                )

            }
            Text(
                text = "$timeLeft",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
