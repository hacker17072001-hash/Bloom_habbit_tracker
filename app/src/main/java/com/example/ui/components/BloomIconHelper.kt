package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object BloomIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "water" -> Icons.Default.WaterDrop
            "book" -> Icons.AutoMirrored.Filled.MenuBook
            "fitness" -> Icons.Default.FitnessCenter
            "run" -> Icons.AutoMirrored.Filled.DirectionsRun
            "walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
            "spa" -> Icons.Default.SelfImprovement
            "feather", "journal" -> Icons.Default.EditNote
            "code" -> Icons.Default.Code
            "school" -> Icons.Default.School
            "money" -> Icons.Default.AttachMoney
            "heart" -> Icons.Default.Favorite
            "sleep" -> Icons.Default.Bedtime
            "sun" -> Icons.Default.WbSunny
            "flame" -> Icons.Default.LocalFireDepartment
            "star" -> Icons.Default.Star
            "sparkles" -> Icons.Default.AutoAwesome
            "award" -> Icons.Default.WorkspacePremium
            "trophy" -> Icons.Default.EmojiEvents
            "timer" -> Icons.Default.Timer
            else -> Icons.Default.CheckCircle
        }
    }

    val availableIcons: List<Pair<String, String>> = listOf(
        "water" to "Water",
        "book" to "Reading",
        "fitness" to "Fitness",
        "spa" to "Meditation",
        "feather" to "Journal",
        "run" to "Running",
        "walk" to "Walking",
        "code" to "Coding",
        "school" to "Study",
        "sleep" to "Sleep",
        "money" to "Finance",
        "heart" to "Health",
        "star" to "Focus"
    )

    val availableColors = listOf(
        "#6C5CE7", // Primary Violet
        "#36CFA0", // Mint Green
        "#FFB86C", // Orange Warm
        "#FF7480", // Coral Pink
        "#00CEC9", // Cyan Fresh
        "#FD79A8", // Soft Rose
        "#FDCB6E", // Yellow Sun
        "#A29BFE", // Lavender
        "#0984E3"  // Blue Sky
    )

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF6C5CE7)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorInt)
            } else if (cleanHex.length == 8) {
                Color(colorInt)
            } else {
                defaultColor
            }
        } catch (_: Exception) {
            defaultColor
        }
    }
}
