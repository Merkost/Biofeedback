package com.merkost.biofeedback.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.healbe.healbesdk.device_api.ClientState
import com.merkost.biofeedback.R
import com.merkost.biofeedback.data.HealBeRepository
import com.merkost.biofeedback.ui.component.DoubleBackPressHandler
import com.merkost.biofeedback.viewmodel.VisualizationViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    navigateToOptions: () -> Unit,
    navigateToConnectionSetting: () -> Unit,
) {
    val healbeRepository = koinInject<HealBeRepository>()
    var selectedOption: BiofeedbackOption? by remember {
        mutableStateOf(null)
    }

    DoubleBackPressHandler()

    val isHealBeConnected by healbeRepository.isHealBeReady.collectAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                IconButton(onClick = { navigateToConnectionSetting() }) {
                    Icon(Icons.Default.Link, "")
                }
            })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.biofeedback),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(R.string.welcome),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OptionsView(selectedOption, onOptionSelected = { selectedOption = it })

            Button(
                enabled = selectedOption != null && isHealBeConnected,
                onClick = navigateToOptions, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = stringResource(R.string.start), fontSize = 18.sp)
            }
        }
    }
}

fun Context.showToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}
