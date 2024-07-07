package com.merkost.biofeedback.ui.healbe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healbe.healbesdk.business_api.HealbeSdk

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var login by remember {
        mutableStateOf("podkupniak.mo@dvfu.ru")
    }

    var password by remember {
        mutableStateOf("Mar11a@mariia")
    }

    Scaffold {
        Column(
            modifier = Modifier.padding(it).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text(
                "Log in to Healbe",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(value = login,
                    onValueChange = { login = it },
                    label = { Text("Login") })
                OutlinedTextField(value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") })

                Button(onClick = {
                    HealbeSdk.init(context)
                }) {
                    Text("Confirm")
                }

            }
        }
    }
}