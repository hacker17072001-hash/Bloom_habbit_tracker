package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class BloomNavDestination(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    HABITS("Habits", Icons.Default.CheckCircle),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    TIMER("Timer", Icons.Default.Timer),
    ACHIEVEMENTS("Badges", Icons.Default.EmojiEvents),
    SETTINGS("Settings", Icons.Default.Settings)
}
