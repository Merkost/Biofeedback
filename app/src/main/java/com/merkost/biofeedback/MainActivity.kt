package com.merkost.biofeedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healbe.complex_example.SplashActivity
import com.merkost.biofeedback.data.HealbeExceptionHandler
import com.merkost.biofeedback.ui.BaselineScreen
import com.merkost.biofeedback.ui.ConnectionSettingsScreen
import com.merkost.biofeedback.ui.Screen
import com.merkost.biofeedback.ui.VisualizationScreen
import com.merkost.biofeedback.ui.WelcomeScreen
import com.merkost.biofeedback.ui.healbe.LoginScreen
import com.merkost.biofeedback.ui.showToast
import com.merkost.biofeedback.ui.theme.BiofeedbackTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiofeedbackTheme {
                val navController = rememberNavController()
                Navigation(navController = navController)
            }
        }
    }
}

@Composable
fun Navigation(navController: NavHostController) {


    val exceptionHandler = koinInject<HealbeExceptionHandler>()

    val exceptions = exceptionHandler.exceptionsFlow.collectAsState(null)

    LaunchedEffect(exceptions.value) {
        exceptions.value?.let {
            navController.context.showToast(it.exception.message ?: it.message)
        }
    }

    NavHost(navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navigateToOptions = {
                    navController.navigate(Screen.BaselineMeasurement.route)
                },
                navigateToConnectionSetting = {
                    navController.navigate(Screen.ConnectionSettings.route)
                }
            )
        }

        composable(Screen.ConnectionSettings.route) {
            val context = navController.context

            ConnectionSettingsScreen(
                onBack = navController::popBackStack,
                onHealBeConnect = {
                    startHealbe(context)
                }
            )
        }

        composable(Screen.BaselineMeasurement.route) {
            BaselineScreen(
                onComplete = {
                    navController.navigate(Screen.Visualization.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                }
            )
        }

        composable(Screen.Visualization.route) {
            VisualizationScreen()
        }

        composable(Screen.HealbeLogin.route) {
            LoginScreen()
        }
    }
}

fun startHealbe(context: Context) {
    val intent = Intent(context, SplashActivity::class.java)
    if (context !is AppCompatActivity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
