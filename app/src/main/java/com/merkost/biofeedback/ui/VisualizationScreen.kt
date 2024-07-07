package com.merkost.biofeedback.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.merkost.biofeedback.R
import com.merkost.biofeedback.viewmodel.VisualizationViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizationScreen(viewModel: VisualizationViewModel = koinViewModel()) {
    val heartRate by viewModel.heartRateData.collectAsState()
    val heartRates by viewModel.heartRates.collectAsState()
    val gsrRate by viewModel.gsrData.collectAsState()
    val gsrRates by viewModel.gsrRates.collectAsState()
    val gobeVersion by viewModel.gobeVersion.collectAsState()
    val shortSummaryInfo by viewModel.shortSummaryInfo.collectAsState()
    val isConnected by viewModel.healbeConnected.collectAsState()

    val minHeartRate = 60
    val maxHeartRate = 100

    var showRaw by remember {
        mutableStateOf(false)
    }

    val balloonPosition by animateFloatAsState(
        animationSpec = tween(5500),
        targetValue = ((maxHeartRate - heartRate).toFloat() / (maxHeartRate - minHeartRate).toFloat()).coerceIn(
            -0.5f,
            1.0f
        )
    )

    val skyComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.windmill_pink))
    val skyProgress by animateLottieCompositionAsState(
        composition = skyComposition,
        iterations = LottieConstants.IterateForever
    )

    if (showRaw) {
        ModalBottomSheet(
            onDismissRequest = {
                showRaw = false
            },
            content = {
                RawDataVisualizationScreen(
                    isConnected = isConnected,
                    gobeVersion = gobeVersion,
                    summaryInfo = shortSummaryInfo,
                    currentHeartRate = heartRate,
                    currentGsrActivity = gsrRate,
                    heartRates = heartRates,
                    gsrValues = gsrRates
                )
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = skyComposition,
                progress = { skyProgress },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        showRaw = true
                    },
                contentScale = ContentScale.Crop
            )

            val offsetY = with(LocalDensity.current) { (balloonPosition * 300).dp }
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.balloon))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            if (heartRates.isNotEmpty()) {

                LottieAnimation(
                    composition = composition,
                    progress = { progress }, // Pass the progress to the LottieAnimation
                    modifier = Modifier
                        .size(500.dp)
                        .offset(y = -offsetY)
                )

            }


//            Button(
//                onClick = { showRaw = true },
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(16.dp)
//            ) {
//                Text(text = "Raw Data")
//            }
        }
    }
}



