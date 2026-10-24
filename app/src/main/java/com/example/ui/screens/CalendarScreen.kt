package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.model.HabitItem
import com.example.ui.components.BloomHabitCard
import com.example.ui.theme.BloomError
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSecondary
import com.example.ui.theme.BloomSoftLavender
import com.example.ui.theme.BloomSuccess
import com.example.ui.theme.BloomWarning
import com.example.util.DateUtils
import com.example.util.HabitCalculations

@Composable
fun CalendarScreen(
    activeHabits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onToggleHabit: (HabitEntity, String) -> Unit,
    onAdjustProgress: (HabitEntity, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableIntStateOf(2) } // 0: Daily, 1: Weekly, 2: Monthly, 3: Yearly

    val (selYear, selMonth, selDay) = DateUtils.parseParts(selectedDate)
    var viewYear by remember { mutableIntStateOf(selYear) }
    var viewMonth by remember { mutableIntStateOf(selMonth) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // Screen Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Habit Calendar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Review and track historical consistency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mode Switcher Tabs (Daily, Weekly, Monthly, Yearly)
        TabRow(
            selectedTabIndex = viewMode,
            containerColor = Color.Transparent,
            contentColor = BloomPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[viewMode]),
                    color = BloomPrimary
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            listOf("Daily", "Weekly", "Monthly", "Yearly").forEachIndexed { index, title ->
                Tab(
                    selected = viewMode == index,
                    onClick = { viewMode = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (viewMode) {
            0 -> DailyCalendarView(
                selectedDate = selectedDate,
                activeHabits = activeHabits,
                logs = logs,
                onSelectDate = onSelectDate,
                onToggleHabit = { onToggleHabit(it, selectedDate) },
                onAdjustProgress = { habit, delta -> onAdjustProgress(habit, delta, selectedDate) }
            )
            1 -> WeeklyCalendarView(
                selectedDate = selectedDate,
                activeHabits = activeHabits,
                logs = logs,
                onSelectDate = onSelectDate,
                onToggleHabit = onToggleHabit
            )
            2 -> MonthlyCalendarView(
                year = viewYear,
                month = viewMonth,
                selectedDate = selectedDate,
                activeHabits = activeHabits,
                logs = logs,
                onSelectDate = onSelectDate,
                onPrevMonth = {
                    if (viewMonth == 1) {
                        viewYear--
                        viewMonth = 12
                    } else {
                        viewMonth--
                    }
                },
                onNextMonth = {
                    if (viewMonth == 12) {
                        viewYear++
                        viewMonth = 1
                    } else {
                        viewMonth++
                    }
                },
                onToggleHabit = { onToggleHabit(it, selectedDate) },
                onAdjustProgress = { habit, delta -> onAdjustProgress(habit, delta, selectedDate) }
            )
            3 -> YearlyHeatmapView(
                year = viewYear,
                activeHabits = activeHabits,
                logs = logs,
                onSelectDate = {
                    onSelectDate(it)
                    viewMode = 0 // jump to daily view
                }
            )
        }
    }
}

@Composable
fun MonthlyCalendarView(
    year: Int,
    month: Int,
    selectedDate: String,
    activeHabits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onSelectDate: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleHabit: (HabitEntity) -> Unit,
    onAdjustProgress: (HabitEntity, Double) -> Unit
) {
    val daysInMonth = DateUtils.getDaysInMonth(year, month)
    val firstDayOffset = DateUtils.getFirstDayOfWeekForMonth(year, month) - 1 // 0 for Mon
    val logsByDate = logs.groupBy { it.localDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        IconButton(onClick = onPrevMonth) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }
                        Text(
                            text = DateUtils.formatMonthYear(year, month),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onNextMonth) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid (6 rows max)
                    val totalSlots = firstDayOffset + daysInMonth
                    val rows = (totalSlots + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0..6) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - firstDayOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateStr = DateUtils.formatDate(year, month, dayNum)
                                    val isSelected = dateStr == selectedDate
                                    val isToday = dateStr == DateUtils.today()

                                    val scheduledForDay = activeHabits.filter { HabitCalculations.isHabitScheduledOn(it, dateStr) }
                                    val logsForDay = logsByDate[dateStr]?.associateBy { it.habitId } ?: emptyMap()
                                    val completedCount = scheduledForDay.count { h ->
                                        val l = logsForDay[h.id]
                                        l?.completionStatus == "COMPLETED" || (h.targetValue > 0 && (l?.progressValue ?: 0.0) >= h.targetValue)
                                    }

                                    val dotColor = when {
                                        scheduledForDay.isEmpty() -> Color.Transparent
                                        completedCount >= scheduledForDay.size && completedCount > 0 -> BloomSuccess
                                        completedCount > 0 -> BloomWarning
                                        dateStr < DateUtils.today() -> BloomError.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }

                                    Column(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) BloomPrimary else if (isToday) BloomSoftLavender else Color.Transparent
                                            )
                                            .clickable { onSelectDate(dateStr) },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else dotColor)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendItem(color = BloomSuccess, label = "Completed")
                        LegendItem(color = BloomWarning, label = "Partial")
                        LegendItem(color = BloomError.copy(alpha = 0.5f), label = "Missed")
                        LegendItem(color = MaterialTheme.colorScheme.outlineVariant, label = "Rest")
                    }
                }
            }
        }

        // Daily Detail Section for Selected Date
        item {
            val scheduledForSel = activeHabits.filter { HabitCalculations.isHabitScheduledOn(it, selectedDate) }
            val logsForSel = logsByDate[selectedDate]?.associateBy { it.habitId } ?: emptyMap()

            Text(
                text = "${DateUtils.formatFullDisplay(selectedDate)} Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (scheduledForSel.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No habits scheduled on this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                scheduledForSel.forEach { habit ->
                    val habitItem = HabitItem(
                        habit = habit,
                        todayLog = logsForSel[habit.id],
                        currentStreak = 0,
                        isScheduledToday = true
                    )
                    BloomHabitCard(
                        habitItem = habitItem,
                        onToggle = { onToggleHabit(habit) },
                        onAdjustProgress = { delta -> onAdjustProgress(habit, delta) },
                        onClick = { onToggleHabit(habit) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyCalendarView(
    selectedDate: String,
    activeHabits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onSelectDate: (String) -> Unit,
    onToggleHabit: (HabitEntity) -> Unit,
    onAdjustProgress: (HabitEntity, Double) -> Unit
) {
    val logsByDate = logs.groupBy { it.localDate }
    val logsForDay = logsByDate[selectedDate]?.associateBy { it.habitId } ?: emptyMap()
    val scheduledHabits = activeHabits.filter { HabitCalculations.isHabitScheduledOn(it, selectedDate) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Navigation bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSelectDate(DateUtils.addDays(selectedDate, -1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateUtils.formatFullDisplay(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedDate == DateUtils.today()) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = BloomPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = { onSelectDate(DateUtils.addDays(selectedDate, 1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }
        }

        if (scheduledHabits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits scheduled for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(scheduledHabits, key = { it.id }) { habit ->
                val habitItem = HabitItem(
                    habit = habit,
                    todayLog = logsForDay[habit.id],
                    currentStreak = 0,
                    isScheduledToday = true
                )
                BloomHabitCard(
                    habitItem = habitItem,
                    onToggle = { onToggleHabit(habit) },
                    onAdjustProgress = { delta -> onAdjustProgress(habit, delta) },
                    onClick = { onToggleHabit(habit) }
                )
            }
        }
    }
}

@Composable
fun WeeklyCalendarView(
    selectedDate: String,
    activeHabits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onSelectDate: (String) -> Unit,
    onToggleHabit: (HabitEntity, String) -> Unit
) {
    val logsByDate = logs.groupBy { it.localDate }
    val isoDay = DateUtils.getIsoDayOfWeek(selectedDate)
    val monOffset = 1 - isoDay
    val weekDates = (0..6).map { DateUtils.addDays(selectedDate, monOffset + it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Date Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSelectDate(DateUtils.addDays(selectedDate, -7)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week")
                    }
                    Text(
                        text = "${DateUtils.formatDisplay(weekDates.first())} – ${DateUtils.formatDisplay(weekDates.last())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onSelectDate(DateUtils.addDays(selectedDate, 7)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week")
                    }
                }
            }
        }

        // Matrix of Habits x 7 days
        items(activeHabits, key = { it.id }) { habit ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDates.forEach { dStr ->
                            val isScheduled = HabitCalculations.isHabitScheduledOn(habit, dStr)
                            val log = logsByDate[dStr]?.find { it.habitId == habit.id }
                            val isCompleted = log?.completionStatus == "COMPLETED" ||
                                    (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
                            val isToday = dStr == DateUtils.today()
                            val iso = DateUtils.getIsoDayOfWeek(dStr)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = isScheduled) { onToggleHabit(habit, dStr) }
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = DateUtils.getWeekdayName(iso),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                !isScheduled -> Color.Transparent
                                                isCompleted -> BloomSuccess
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else if (!isScheduled) 1.dp else 0.dp,
                                            color = if (isToday) BloomPrimary else if (!isScheduled) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YearlyHeatmapView(
    year: Int,
    activeHabits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onSelectDate: (String) -> Unit
) {
    val logsByDate = logs.groupBy { it.localDate }
    val today = DateUtils.today()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "$year Contribution Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visual consistency map across the year",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Months (1 to 12)
                    for (m in 1..12) {
                        Text(
                            text = DateUtils.formatMonthYear(year, m),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BloomPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val daysCount = DateUtils.getDaysInMonth(year, m)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (d in 1..daysCount) {
                                val dStr = DateUtils.formatDate(year, m, d)
                                val scheduled = activeHabits.filter { HabitCalculations.isHabitScheduledOn(it, dStr) }
                                val dayLogs = logsByDate[dStr]?.associateBy { it.habitId } ?: emptyMap()
                                val comp = scheduled.count { h ->
                                    val l = dayLogs[h.id]
                                    l?.completionStatus == "COMPLETED" || (h.targetValue > 0 && (l?.progressValue ?: 0.0) >= h.targetValue)
                                }
                                val ratio = if (scheduled.isNotEmpty()) comp.toFloat() / scheduled.size else 0f

                                val cellColor = when {
                                    dStr > today -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ratio >= 1f -> BloomPrimary
                                    ratio >= 0.5f -> BloomSecondary
                                    ratio > 0f -> BloomSoftLavender
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(cellColor)
                                        .clickable { onSelectDate(dStr) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
