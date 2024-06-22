package com.merkost.biofeedback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.merkost.biofeedback.ui.BaselineScreen
import com.merkost.biofeedback.ui.VisualizationScreen
import com.merkost.biofeedback.ui.Screen
import com.merkost.biofeedback.ui.WelcomeScreen
import com.merkost.biofeedback.ui.theme.BiofeedbackTheme

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
    NavHost(navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navigateToOptions = {
                navController.navigate(Screen.BaselineMeasurement.route)
            })
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
    }
}