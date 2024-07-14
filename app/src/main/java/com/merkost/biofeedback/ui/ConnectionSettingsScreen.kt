package com.merkost.biofeedback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healbe.healbesdk.device_api.ClientState
import com.merkost.biofeedback.R
import com.merkost.biofeedback.data.HealBeRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(onBack: () -> Unit, onHealBeConnect: () -> Unit) {

    val context = LocalContext.current

    val healBeRepository = koinInject<HealBeRepository>()
    val coroutineScope = rememberCoroutineScope()

    val isSdkInitialized by healBeRepository.isSdkInitialized.collectAsState()
    val healBeConnectionState by healBeRepository.healBeConnectionState.collectAsState(
        initial = ClientState.DISCONNECTED
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.connection_settings))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
        ) {
            SettingColumn {
                SettingTitle(text = "Sdk Initialized: $isSdkInitialized")
                SettingButton(text = "Initialize", onClick = {
                    coroutineScope.launch {
                        healBeRepository.initSdk()
                    }
                })
            }

            SettingColumn {
                SettingTitle(text = "GoBe Connection: ${healBeConnectionState.name}")
                SettingButton(text = "Connect", onClick = {
                    coroutineScope.launch {
                        healBeRepository.connectToGoBe()
                    }
                }
                )
            }

            SettingColumn {
                SettingButton(text = "Start healBe SDK connection guide") {
                    onHealBeConnect()
                }
            }
        }
    }
}

@Composable
fun SettingButton(text: String, onClick: () -> Unit) {
    Button(onClick) { Text(text) }
}

@Composable
fun SettingTitle(text: String) {
    Text(text)
}

@Composable
fun SettingColumn(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}