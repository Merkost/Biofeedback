package com.merkost.biofeedback.ui

import GsrGraph
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.biofeedback.R
import com.merkost.biofeedback.ui.component.HeartRateGraph

@Composable
fun RawDataSimpleScreen(
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
        Text(
            text = "Current Heart Rate: $currentHeartRate",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Current GSR: $currentGsrActivity",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        if (heartRates.isNotEmpty()) {
            HeartRateGraph(
                heartRates = heartRates
            )
        }
        if (gsrValues.isNotEmpty()) {
            GsrGraph(
                gsrValues = gsrValues
            )
        }
    }
}
