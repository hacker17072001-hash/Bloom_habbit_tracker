package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.model.BloomDashboardState
import com.example.model.DayProgress
import com.example.model.HabitItem
import com.example.ui.components.BloomCircularProgressCard
import com.example.ui.components.BloomHabitCard
import com.example.ui.components.BloomKpiCard
import com.example.ui.components.BloomStreakCard
import com.example.ui.theme.BloomGold
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSecondary
import com.example.ui.theme.BloomSoftLavender
import com.example.ui.theme.BloomSuccess

@Composable
fun DashboardScreen(
    dashboardState: BloomDashboardState,
    onToggleHabit: (HabitEntity) -> Unit,
    onAdjustProgress: (HabitEntity, Double) -> Unit,
    onHabitClick: (HabitEntity) -> Unit,
    onAddHabitClick: () -> Unit,
    onSeedStarterHabits: () -> Unit,
    onSelectDate: (String) -> Unit,
    onLaunchTimer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Greeting & Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dashboardState.greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dashboardState.motivationalQuote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Profile Avatar with Initials
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BloomSoftLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TL",
                        color = BloomPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Streak Card
        item {
            BloomStreakCard(
                currentStreak = dashboardState.currentStreak,
                longestStreak = dashboardState.longestStreak
            )
        }

        // Weekly Overview Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${dashboardState.weeklyCompletionRate.toInt()}% achieved",
                            style = MaterialTheme.typography.labelSmall,
                            color = BloomPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dashboardState.weekDays.forEach { dayProg ->
                            WeekDayPill(
                                dayProgress = dayProg,
                                onClick = { onSelectDate(dayProg.date) }
                            )
                        }
                    }
                }
            }
        }

        // Circular Progress Widget
        item {
            BloomCircularProgressCard(
                percentage = dashboardState.todayPercentage,
                completedCount = dashboardState.completedToday,
                remainingCount = dashboardState.remainingToday
            )
        }

        // KPI Summary Grid (4 cards in 2 rows)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BloomKpiCard(
                    title = "Active Habits",
                    value = "${dashboardState.totalActiveHabits}",
                    subtitle = "Tracked daily",
                    icon = Icons.Default.CheckCircle,
                    iconTint = BloomPrimary,
                    iconBg = BloomSoftLavender,
                    modifier = Modifier.weight(1f)
                )
                BloomKpiCard(
                    title = "Today Done",
                    value = "${dashboardState.completedToday}",
                    subtitle = "of ${dashboardState.todayHabits.size} scheduled",
                    icon = Icons.Default.PieChart,
                    iconTint = BloomSuccess,
                    iconBg = BloomSuccess.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BloomKpiCard(
                    title = "Success Rate",
                    value = "${dashboardState.todayPercentage.toInt()}%",
                    subtitle = "Today's pace",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFE17055),
                    iconBg = Color(0xFFFFECEE),
                    modifier = Modifier.weight(1f)
                )
                BloomKpiCard(
                    title = "Total Logs",
                    value = "${dashboardState.totalSuccessfulCompletions}",
                    subtitle = "All-time wins",
                    icon = Icons.Default.WorkspacePremium,
                    iconTint = BloomGold,
                    iconBg = BloomGold.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Habits Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${dashboardState.completedToday} of ${dashboardState.todayHabits.size} completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onAddHabitClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloomSoftLavender),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_habit_quick_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = BloomPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Habit",
                            color = BloomPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Empty state when no habits exist
        if (dashboardState.todayHabits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "🌱",
                            fontSize = 44.sp
                        )
                        Text(
                            text = "Build better habits, one day at a time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create custom habits or start instantly with our thoughtful starter routines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAddHabitClick,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BloomPrimary)
                            ) {
                                Text("Create Habit", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onSeedStarterHabits,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Starter Pack", color = BloomPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            // Habit cards
            items(dashboardState.todayHabits, key = { it.habit.id }) { habitItem ->
                BloomHabitCard(
                    habitItem = habitItem,
                    onToggle = { onToggleHabit(habitItem.habit) },
                    onAdjustProgress = { delta -> onAdjustProgress(habitItem.habit, delta) },
                    onClick = { onHabitClick(habitItem.habit) },
                    onLaunchTimer = if (habitItem.habit.type == "DURATION") {
                        { onLaunchTimer(habitItem.habit.id) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun WeekDayPill(
    dayProgress: DayProgress,
    onClick: () -> Unit
) {
    val isToday = dayProgress.isToday
    val isCompleted = dayProgress.scheduledCount > 0 && dayProgress.completedCount >= dayProgress.scheduledCount
    val isPartial = dayProgress.completedCount > 0 && dayProgress.completedCount < dayProgress.scheduledCount

    val bgTint = when {
        isToday -> BloomPrimary
        isCompleted -> BloomSuccess.copy(alpha = 0.15f)
        isPartial -> Color(0xFFFFB86C).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isToday -> Color.White
        isCompleted -> BloomSuccess
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .width(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgTint)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayProgress.dayName,
            fontSize = 11.sp,
            color = if (isToday) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${dayProgress.dayNumber}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status indicator dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isToday -> Color.White
                        isCompleted -> BloomSuccess
                        isPartial -> Color(0xFFFFB86C)
                        dayProgress.scheduledCount == 0 -> Color.Transparent
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                )
        )
    }
}
