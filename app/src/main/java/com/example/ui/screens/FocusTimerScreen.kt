package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSecondary
import com.example.ui.theme.BloomSoftLavender
import com.example.ui.theme.BloomSuccess
import com.example.ui.viewmodel.TimerUiState

@Composable
fun FocusTimerScreen(
    timerState: TimerUiState,
    habits: List<HabitEntity>,
    onSetDuration: (Int) -> Unit,
    onSetHabit: (Long) -> Unit,
    onSetAmbientSound: (String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onFinishTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(5, 10, 15, 25, 30, 45, 60)
    val ambientOptions = listOf("None", "Rain", "Forest", "Waves")

    val durationHabits = habits.filter { it.type == "DURATION" || !it.isArchived }

    val totalSecs = (timerState.initialMinutes * 60).coerceAtLeast(1)
    val fraction = if (timerState.isCountUp) {
        ((timerState.elapsedSeconds % 3600) / 3600f).coerceIn(0f, 1f)
    } else {
        (timerState.remainingSeconds.toFloat() / totalSecs).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    val minutes = if (timerState.isCountUp) timerState.elapsedSeconds / 60 else timerState.remainingSeconds / 60
    val seconds = if (timerState.isCountUp) timerState.elapsedSeconds % 60 else timerState.remainingSeconds % 60
    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_timer_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Focus Timer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Deep focus sessions add time directly to your habit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Circular Timer Display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val primaryColor = BloomPrimary
                    val secondaryColor = BloomSecondary

                    Box(
                        modifier = Modifier.size(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(240.dp)) {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Background circle
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Animated active sweep
                            if (animatedProgress > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        0f to primaryColor,
                                        0.5f to secondaryColor,
                                        1f to primaryColor
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Time Readout
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 2.sp
                            )
                            val selHabit = habits.find { it.id == timerState.selectedHabitId }
                            Text(
                                text = selHabit?.name ?: "General Focus",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BloomPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Controls Row: Reset, Play/Pause, Finish
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset
                        IconButton(
                            onClick = onResetTimer,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        }

                        // Play / Pause
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BloomPrimary)
                                .clickable {
                                    if (timerState.isRunning) onPauseTimer() else onStartTimer()
                                }
                                .testTag("timer_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (timerState.isRunning) "Pause" else "Start",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Finish Session
                        IconButton(
                            onClick = onFinishTimer,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BloomSuccess.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Done, contentDescription = "Finish", tint = BloomSuccess)
                        }
                    }
                }
            }
        }

        // Preset Duration Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preset Durations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { mins ->
                        FilterChip(
                            selected = timerState.initialMinutes == mins,
                            onClick = { onSetDuration(mins) },
                            label = { Text("${mins}m") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BloomSoftLavender,
                                selectedLabelColor = BloomPrimary
                            )
                        )
                    }
                }
            }
        }

        // Target Habit Selector
        if (durationHabits.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Link to Habit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = timerState.selectedHabitId == 0L,
                                onClick = { onSetHabit(0L) },
                                label = { Text("None (General)") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BloomSoftLavender,
                                    selectedLabelColor = BloomPrimary
                                )
                            )
                        }
                        items(durationHabits) { habit ->
                            FilterChip(
                                selected = timerState.selectedHabitId == habit.id,
                                onClick = { onSetHabit(habit.id) },
                                label = { Text(habit.name) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BloomSoftLavender,
                                    selectedLabelColor = BloomPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Ambient Sound Scene Picker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Sound",
                            tint = BloomPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ambient Sound Scene",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ambientOptions.forEach { sound ->
                            FilterChip(
                                selected = timerState.ambientSound == sound,
                                onClick = { onSetAmbientSound(sound) },
                                label = { Text(sound, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BloomSoftLavender,
                                    selectedLabelColor = BloomPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
