package com.merkost.biofeedback.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.ui.graphics.vector.ImageVector
import com.merkost.biofeedback.R

enum class BiofeedbackOption(val icon: ImageVector, val nameRes: Int) {
    HEART(Icons.Default.Favorite, R.string.heart_rate),
    STRESS(Icons.Default.Mood, R.string.stress),
}