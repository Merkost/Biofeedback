package com.merkost.biofeedback.ui

import GsrGraph
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.biofeedback.R
import com.merkost.biofeedback.data.ShortSummaryInfo
import com.merkost.biofeedback.ui.component.HeartRateGraph

@Composable
fun RawDataVisualizationScreen(
    isConnected: Boolean,
    gobeVersion: String,
    summaryInfo: ShortSummaryInfo?,
    currentHeartRate: Int,
    currentGsrActivity: Int,
    heartRates: List<Int>,
    gsrValues: List<Int>,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.data_statistic),
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        AnimatedHeartIcon(
            modifier = Modifier.size(25.dp),
            heartRate = currentHeartRate
        )

//        Text(
//            text = "Is connected: $isConnected",
//            fontSize = 16.sp,
//            color = MaterialTheme.colorScheme.primary
//        )

        Text(
            text = stringResource(R.string.current_heart_rate, currentHeartRate),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

//        Text(
//            text = "Current GSR: $currentGsrActivity",
//            fontSize = 20.sp,
//            color = MaterialTheme.colorScheme.primary
//        )
        if (heartRates.isNotEmpty()) {
            HeartRateGraph(
                heartRates = heartRates
            )
        }
//        if (gsrValues.isNotEmpty()) {
//            GsrGraph(
//                gsrValues = gsrValues
//            )
//        }

        summaryInfo?.let {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Connection Status: ${if (summaryInfo.isConnected) "Connected" else "Disconnected"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "On Hand: ${if (summaryInfo.isOnHand) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Heart Rate: ${summaryInfo.heartRate.heartRate} bpm",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Neuro Data: ${summaryInfo.neuroData.gsrActivity}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Gobe version: $gobeVersion",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }


    }
}

@Composable
fun AnimatedHeartIcon(modifier: Modifier, heartRate: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Image(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Heart Icon",
        modifier = modifier
            .scale(scale),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Red)
    )
}
