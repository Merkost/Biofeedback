package com.merkost.biofeedback.ui

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object BaselineMeasurement : Screen("baseline_measurement")
    data object Visualization : Screen("visualization")
    data object HealbeLogin: Screen("healbe_login")
}