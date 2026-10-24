package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.ui.components.HabitFormDialog
import com.example.ui.navigation.BloomNavDestination
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSoftLavender
import com.example.ui.theme.BloomTheme
import com.example.ui.viewmodel.BloomViewModel

@Composable
fun BloomApp(viewModel: BloomViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    BloomTheme(darkTheme = isDark) {
        var currentDestination by remember { mutableStateOf(BloomNavDestination.DASHBOARD) }
        var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }
        var showHabitDialog by remember { mutableStateOf(false) }

        // BackHandler to return to Dashboard if on other screens
        BackHandler(enabled = currentDestination != BloomNavDestination.DASHBOARD) {
            currentDestination = BloomNavDestination.DASHBOARD
        }

        val dashboardState by viewModel.dashboardState.collectAsState()
        val activeHabits by viewModel.activeHabits.collectAsState()
        val archivedHabits by viewModel.archivedHabits.collectAsState()
        val categories by viewModel.categories.collectAsState()
        val achievements by viewModel.achievements.collectAsState()
        val timerState by viewModel.timerState.collectAsState()
        val analyticsState by viewModel.analyticsState.collectAsState()
        val analyticsFilter by viewModel.selectedAnalyticsFilter.collectAsState()
        val selectedDate by viewModel.selectedDate.collectAsState()
        val allLogs by viewModel.allLogs.collectAsState()
        val reflection by viewModel.currentReflection.collectAsState()
        val userName by viewModel.userName.collectAsState()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrDesktop = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!isTabletOrDesktop) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            val mainTabs = listOf(
                                BloomNavDestination.DASHBOARD,
                                BloomNavDestination.HABITS,
                                BloomNavDestination.CALENDAR,
                                BloomNavDestination.ANALYTICS,
                                BloomNavDestination.TIMER,
                                BloomNavDestination.SETTINGS
                            )
                            mainTabs.forEach { dest ->
                                val selected = currentDestination == dest
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BloomPrimary,
                                        selectedTextColor = BloomPrimary,
                                        indicatorColor = BloomSoftLavender,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Left Navigation Rail for Tablet / Desktop
                    if (isTabletOrDesktop) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                ) {
                                    Text("🌱", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Bloom",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BloomPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            BloomNavDestination.entries.forEach { dest ->
                                val selected = currentDestination == dest
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.title
                                        )
                                    },
                                    label = { Text(dest.title, fontSize = 11.sp) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = BloomPrimary,
                                        selectedTextColor = BloomPrimary,
                                        indicatorColor = BloomSoftLavender
                                    )
                                )
                            }
                        }
                    }

                    // Main Content Canvas
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { destination ->
                            when (destination) {
                                BloomNavDestination.DASHBOARD -> DashboardScreen(
                                    dashboardState = dashboardState,
                                    onToggleHabit = { habit -> viewModel.toggleHabit(habit) },
                                    onAdjustProgress = { habit, delta -> viewModel.adjustProgress(habit, delta) },
                                    onHabitClick = { habit ->
                                        habitToEdit = habit
                                        showHabitDialog = true
                                    },
                                    onAddHabitClick = {
                                        habitToEdit = null
                                        showHabitDialog = true
                                    },
                                    onSeedStarterHabits = { viewModel.seedStarterHabits() },
                                    onSelectDate = { date ->
                                        viewModel.setSelectedDate(date)
                                        currentDestination = BloomNavDestination.CALENDAR
                                    },
                                    onLaunchTimer = { habitId ->
                                        viewModel.setTimerHabit(habitId)
                                        currentDestination = BloomNavDestination.TIMER
                                    }
                                )

                                BloomNavDestination.HABITS -> HabitsScreen(
                                    activeHabits = activeHabits,
                                    archivedHabits = archivedHabits,
                                    categories = categories,
                                    onAddHabit = {
                                        habitToEdit = null
                                        showHabitDialog = true
                                    },
                                    onEditHabit = { habit ->
                                        habitToEdit = habit
                                        showHabitDialog = true
                                    },
                                    onArchiveHabit = { id, archived -> viewModel.archiveHabit(id, archived) },
                                    onDeleteHabit = { id, deleteLogs -> viewModel.deleteHabit(id, deleteLogs) }
                                )

                                BloomNavDestination.CALENDAR -> CalendarScreen(
                                    activeHabits = activeHabits,
                                    logs = allLogs,
                                    selectedDate = selectedDate,
                                    onSelectDate = { viewModel.setSelectedDate(it) },
                                    onToggleHabit = { habit, date -> viewModel.toggleHabit(habit, date) },
                                    onAdjustProgress = { habit, delta, date -> viewModel.adjustProgress(habit, delta, date) }
                                )

                                BloomNavDestination.ANALYTICS -> AnalyticsScreen(
                                    analyticsState = analyticsState,
                                    selectedFilter = analyticsFilter,
                                    onSelectFilter = { viewModel.setAnalyticsFilter(it) }
                                )

                                BloomNavDestination.TIMER -> FocusTimerScreen(
                                    timerState = timerState,
                                    habits = activeHabits,
                                    onSetDuration = { mins -> viewModel.setTimerDuration(mins) },
                                    onSetHabit = { id -> viewModel.setTimerHabit(id) },
                                    onSetAmbientSound = { sound -> viewModel.setAmbientSound(sound) },
                                    onStartTimer = { viewModel.startTimer() },
                                    onPauseTimer = { viewModel.pauseTimer() },
                                    onResetTimer = { viewModel.resetTimer() },
                                    onFinishTimer = { viewModel.finishTimer() }
                                )

                                BloomNavDestination.ACHIEVEMENTS -> AchievementsScreen(
                                    achievements = achievements,
                                    reflection = reflection,
                                    onSaveReflection = { well, diff, imp, mood ->
                                        viewModel.saveReflection(well, diff, imp, mood)
                                    }
                                )

                                BloomNavDestination.SETTINGS -> SettingsScreen(
                                    themeMode = themeMode,
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    userName = userName,
                                    onSetUserName = { viewModel.setUserName(it) },
                                    onExportJson = { viewModel.exportDataJson() },
                                    onImportJson = { json -> viewModel.importDataJson(json) },
                                    onExportCsv = { viewModel.exportDataCsv() },
                                    onResetAll = { viewModel.resetAll() }
                                )
                            }
                        }
                    }
                }
            }

            // Create / Edit Habit Dialog
            if (showHabitDialog) {
                HabitFormDialog(
                    initialHabit = habitToEdit,
                    categories = categories,
                    onDismiss = {
                        showHabitDialog = false
                        habitToEdit = null
                    },
                    onSave = { habit ->
                        viewModel.saveHabit(habit)
                        showHabitDialog = false
                        habitToEdit = null
                    }
                )
            }
        }
    }
}
