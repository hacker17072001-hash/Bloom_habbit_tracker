package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.AchievementEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.DailyReflectionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.repository.BloomRepository
import com.example.model.AnalyticsState
import com.example.model.BloomDashboardState
import com.example.model.DayProgress
import com.example.model.HabitItem
import com.example.util.DateUtils
import com.example.util.HabitCalculations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimerUiState(
    val selectedHabitId: Long = 0L,
    val initialMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isCountUp: Boolean = false,
    val ambientSound: String = "None" // "None", "Rain", "Forest", "Waves"
)

class BloomViewModel(private val repository: BloomRepository) : ViewModel() {

    val activeHabits: StateFlow<List<HabitEntity>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedHabits: StateFlow<List<HabitEntity>> = repository.archivedHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedAnalyticsFilter = MutableStateFlow("Last 30 Days")
    val selectedAnalyticsFilter: StateFlow<String> = _selectedAnalyticsFilter.asStateFlow()

    private val _themeMode = MutableStateFlow("LIGHT") // "LIGHT", "DARK", "SYSTEM"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _userName = MutableStateFlow("Thao Lee")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    val currentReflection: StateFlow<DailyReflectionEntity?> = _selectedDate
        .combine(repository.observeReflection(DateUtils.today())) { date, _ ->
            repository.getReflection(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dashboard State
    val dashboardState: StateFlow<BloomDashboardState> = combine(
        repository.activeHabits,
        repository.getLogsBetweenDates(DateUtils.addDays(DateUtils.today(), -30), DateUtils.today()),
        _userName
    ) { habits, logs, name ->
        val today = DateUtils.today()
        val logsByDate = logs.groupBy { it.localDate }
        val todayLogsMap = (logsByDate[today] ?: emptyList()).associateBy { it.habitId }

        val todayHabitItems = habits.filter { HabitCalculations.isHabitScheduledOn(it, today) }
            .map { habit ->
                val habitLogs = logs.filter { it.habitId == habit.id }.associateBy { it.localDate }
                val (curStreak, longStreak) = HabitCalculations.calculateHabitStreak(habit, habitLogs, today)
                HabitItem(
                    habit = habit,
                    todayLog = todayLogsMap[habit.id],
                    currentStreak = curStreak,
                    longestStreak = longStreak,
                    isScheduledToday = true
                )
            }

        val completedToday = todayHabitItems.count { it.isCompleted }
        val totalScheduled = todayHabitItems.size
        val remainingToday = (totalScheduled - completedToday).coerceAtLeast(0)
        val todayPct = if (totalScheduled > 0) (completedToday.toFloat() / totalScheduled) * 100f else 0f

        val (overallStreak, longestOverall) = HabitCalculations.calculateOverallStreak(habits, logs, today)

        // 7-day week progress (Mon - Sun of current week)
        val isoToday = DateUtils.getIsoDayOfWeek(today)
        val monOffset = 1 - isoToday
        val weekDaysList = (0..6).map { i ->
            val dStr = DateUtils.addDays(today, monOffset + i)
            val dIso = DateUtils.getIsoDayOfWeek(dStr)
            val dName = DateUtils.getWeekdayName(dIso)
            val (_, _, dayNum) = DateUtils.parseParts(dStr)
            val scheduledForD = habits.filter { HabitCalculations.isHabitScheduledOn(it, dStr) }
            val logsForD = logsByDate[dStr]?.associateBy { it.habitId } ?: emptyMap()
            val compForD = scheduledForD.count { h ->
                val l = logsForD[h.id]
                l?.completionStatus == "COMPLETED" || (h.targetValue > 0 && (l?.progressValue ?: 0.0) >= h.targetValue)
            }
            val pct = if (scheduledForD.isNotEmpty()) (compForD.toFloat() / scheduledForD.size) * 100f else 0f
            DayProgress(
                date = dStr,
                dayName = dName,
                dayNumber = dayNum,
                scheduledCount = scheduledForD.size,
                completedCount = compForD,
                percentage = pct,
                isToday = dStr == today,
                isFuture = DateUtils.isFutureDate(dStr)
            )
        }

        // Weekly completion rate
        val pastWeekDays = weekDaysList.filter { !it.isFuture && it.scheduledCount > 0 }
        val weeklyRate = if (pastWeekDays.isNotEmpty()) {
            pastWeekDays.sumOf { it.completedCount }.toFloat() / pastWeekDays.sumOf { it.scheduledCount } * 100f
        } else 0f

        val totalSuccessfulCompletions = logs.count { it.completionStatus == "COMPLETED" }

        // Greeting
        val calHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greetingText = when (calHour) {
            in 4..11 -> "Good Morning, $name! 🌱"
            in 12..16 -> "Good Afternoon, $name! ☀️"
            in 17..21 -> "Good Evening, $name! ✨"
            else -> "Hello, $name! 🌙"
        }

        BloomDashboardState(
            greeting = greetingText,
            motivationalQuote = if (overallStreak >= 3) "Incredible streak! Keep up the brilliant focus." else "Every small step brings you closer to your goals.",
            todayDate = DateUtils.formatDisplay(today),
            totalActiveHabits = habits.size,
            completedToday = completedToday,
            remainingToday = remainingToday,
            todayPercentage = todayPct,
            currentStreak = overallStreak,
            longestStreak = longestOverall,
            weeklyCompletionRate = weeklyRate,
            monthlyCompletionRate = 78f,
            totalSuccessfulCompletions = totalSuccessfulCompletions,
            todayHabits = todayHabitItems,
            weekDays = weekDaysList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BloomDashboardState())

    // Analytics State
    val analyticsState: StateFlow<AnalyticsState> = combine(
        repository.activeHabits,
        repository.getLogsBetweenDates(DateUtils.addDays(DateUtils.today(), -365), DateUtils.today()),
        _selectedAnalyticsFilter
    ) { habits, logs, filter ->
        val today = DateUtils.today()
        val daysBack = when (filter) {
            "Last 7 Days" -> 7
            "Last 30 Days" -> 30
            "Last 3 Months" -> 90
            "Last 6 Months" -> 180
            "Current Year" -> 365
            else -> 30
        }
        val startDate = DateUtils.addDays(today, -daysBack)

        val filteredLogs = logs.filter { it.localDate in startDate..today }
        val totalCompletions = filteredLogs.count { it.completionStatus == "COMPLETED" }

        var totalScheduledCount = 0
        var totalCompletedScheduled = 0
        val logsByDateAndHabit = filteredLogs.groupBy { it.localDate }
            .mapValues { it.value.associateBy { l -> l.habitId } }

        var cur = startDate
        val dailyTrend = mutableListOf<Pair<String, Float>>()

        while (cur <= today) {
            val scheduledForDay = habits.filter { HabitCalculations.isHabitScheduledOn(it, cur) }
            val logsForDay = logsByDateAndHabit[cur] ?: emptyMap()
            val compForDay = scheduledForDay.count { h ->
                val l = logsForDay[h.id]
                l?.completionStatus == "COMPLETED" || (h.targetValue > 0 && (l?.progressValue ?: 0.0) >= h.targetValue)
            }
            totalScheduledCount += scheduledForDay.size
            totalCompletedScheduled += compForDay

            val pct = if (scheduledForDay.isNotEmpty()) (compForDay.toFloat() / scheduledForDay.size) * 100f else 0f
            dailyTrend.add(Pair(cur, pct))
            cur = DateUtils.addDays(cur, 1)
        }

        val overallRate = if (totalScheduledCount > 0) (totalCompletedScheduled.toFloat() / totalScheduledCount) * 100f else 0f
        val (curStreak, longStreak) = HabitCalculations.calculateOverallStreak(habits, filteredLogs, today)

        // Weekly bars (group dailyTrend into last 4 weeks)
        val weeklyBars = mutableListOf<Pair<String, Float>>()
        val chunked = dailyTrend.takeLast(28).chunked(7)
        chunked.forEachIndexed { idx, weekData ->
            val avg = if (weekData.isNotEmpty()) weekData.map { it.second }.average().toFloat() else 0f
            weeklyBars.add(Pair("Wk ${idx + 1}", avg))
        }

        val categoryStats = HabitCalculations.calculateCategoryStats(habits, filteredLogs, startDate, today)
        val (topHabits, attentionHabits) = HabitCalculations.calculateHabitRankings(habits, filteredLogs, startDate, today, today)
        val insights = HabitCalculations.generateInsights(overallRate, curStreak, categoryStats.firstOrNull(), attentionHabits.firstOrNull())

        AnalyticsState(
            overallCompletionRate = overallRate,
            currentStreak = curStreak,
            longestStreak = longStreak,
            totalCompletions = totalCompletions,
            dailyTrend = dailyTrend.takeLast(14), // Last 14 days for clean line chart
            weeklyBars = weeklyBars,
            categoryStats = categoryStats,
            topHabits = topHabits.take(5),
            attentionHabits = attentionHabits.take(3),
            localInsights = insights
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    init {
        // Load initial preferences
        viewModelScope.launch {
            _themeMode.value = repository.getPreference("themeMode", "LIGHT")
            _userName.value = repository.getPreference("userName", "Thao Lee")
            repository.evaluateAchievements()
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setAnalyticsFilter(filter: String) {
        _selectedAnalyticsFilter.value = filter
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch { repository.setPreference("themeMode", mode) }
    }

    fun setUserName(name: String) {
        _userName.value = name
        viewModelScope.launch { repository.setPreference("userName", name) }
    }

    fun toggleHabit(habit: HabitEntity, date: String = DateUtils.today()) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habit, date)
        }
    }

    fun adjustProgress(habit: HabitEntity, delta: Double, date: String = DateUtils.today()) {
        viewModelScope.launch {
            repository.adjustHabitProgress(habit, date, delta)
        }
    }

    fun setProgressDirect(habit: HabitEntity, value: Double, date: String = DateUtils.today()) {
        viewModelScope.launch {
            repository.setHabitProgressDirect(habit, date, value)
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.insertHabit(habit)
            } else {
                repository.updateHabit(habit)
            }
        }
    }

    fun archiveHabit(habitId: Long, archived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(habitId, archived)
        }
    }

    fun deleteHabit(habitId: Long, deleteLogs: Boolean = false) {
        viewModelScope.launch {
            repository.deleteHabit(habitId, deleteLogs)
        }
    }

    fun saveReflection(whatWentWell: String, whatWasDifficult: String, whatToImprove: String, mood: String) {
        viewModelScope.launch {
            val reflection = DailyReflectionEntity(
                localDate = _selectedDate.value,
                whatWentWell = whatWentWell,
                whatWasDifficult = whatWasDifficult,
                whatToImprove = whatToImprove,
                mood = mood
            )
            repository.saveReflection(reflection)
        }
    }

    // Focus Timer Operations
    fun setTimerDuration(minutes: Int) {
        _timerState.value = _timerState.value.copy(
            initialMinutes = minutes,
            remainingSeconds = minutes * 60,
            elapsedSeconds = 0,
            isRunning = false
        )
        timerJob?.cancel()
    }

    fun setTimerHabit(habitId: Long) {
        _timerState.value = _timerState.value.copy(selectedHabitId = habitId)
    }

    fun setAmbientSound(sound: String) {
        _timerState.value = _timerState.value.copy(ambientSound = sound)
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning) {
                delay(1000)
                val cur = _timerState.value
                if (cur.isCountUp) {
                    _timerState.value = cur.copy(elapsedSeconds = cur.elapsedSeconds + 1)
                } else {
                    if (cur.remainingSeconds > 1) {
                        _timerState.value = cur.copy(
                            remainingSeconds = cur.remainingSeconds - 1,
                            elapsedSeconds = cur.elapsedSeconds + 1
                        )
                    } else {
                        // Finished
                        finishTimer()
                        break
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val cur = _timerState.value
        _timerState.value = cur.copy(
            remainingSeconds = cur.initialMinutes * 60,
            elapsedSeconds = 0,
            isRunning = false
        )
    }

    fun finishTimer() {
        timerJob?.cancel()
        val cur = _timerState.value
        val elapsed = cur.elapsedSeconds
        _timerState.value = cur.copy(isRunning = false)

        if (elapsed > 0) {
            viewModelScope.launch {
                val session = TimerSessionEntity(
                    habitId = cur.selectedHabitId,
                    localDate = DateUtils.today(),
                    durationSeconds = elapsed.toLong()
                )
                repository.recordTimerSession(session)
            }
        }
    }

    fun seedStarterHabits() {
        viewModelScope.launch {
            val today = DateUtils.today()
            val starters = listOf(
                HabitEntity(
                    name = "Drink 8 Glasses of Water",
                    description = "Stay refreshed and energized throughout the day",
                    iconName = "water",
                    colorHex = "#36CFA0",
                    categoryId = "Health & Fitness",
                    type = "QUANTITY",
                    targetValue = 8.0,
                    unit = "glasses",
                    scheduleType = "EVERY_DAY",
                    preferredTime = "Morning",
                    startDate = today
                ),
                HabitEntity(
                    name = "Read 20 Pages",
                    description = "Expand mind and gain new perspectives",
                    iconName = "book",
                    colorHex = "#FD79A8",
                    categoryId = "Reading",
                    type = "QUANTITY",
                    targetValue = 20.0,
                    unit = "pages",
                    scheduleType = "EVERY_DAY",
                    preferredTime = "Evening",
                    startDate = today
                ),
                HabitEntity(
                    name = "Morning Meditation",
                    description = "Center thoughts with mindful breathing",
                    iconName = "spa",
                    colorHex = "#6C5CE7",
                    categoryId = "Mental Wellness",
                    type = "DURATION",
                    targetValue = 15.0,
                    unit = "mins",
                    scheduleType = "EVERY_DAY",
                    preferredTime = "Morning",
                    startDate = today
                ),
                HabitEntity(
                    name = "Core Workout",
                    description = "Build physical endurance and strength",
                    iconName = "fitness",
                    colorHex = "#FFB86C",
                    categoryId = "Health & Fitness",
                    type = "DURATION",
                    targetValue = 30.0,
                    unit = "mins",
                    scheduleType = "WEEKDAYS",
                    preferredTime = "Afternoon",
                    startDate = today
                ),
                HabitEntity(
                    name = "Evening Reflection & Journal",
                    description = "Review achievements and lessons of the day",
                    iconName = "feather",
                    colorHex = "#A29BFE",
                    categoryId = "Personal Growth",
                    type = "SIMPLE",
                    targetValue = 1.0,
                    unit = "entry",
                    scheduleType = "EVERY_DAY",
                    preferredTime = "Evening",
                    startDate = today
                )
            )
            for (h in starters) {
                repository.insertHabit(h)
            }
        }
    }

    suspend fun exportDataJson(): String = repository.exportJson()

    suspend fun importDataJson(json: String): Boolean = repository.importJson(json)

    suspend fun exportDataCsv(): String = repository.exportCsv()

    suspend fun resetAll() {
        repository.clearAll()
        resetTimer()
    }
}

class BloomViewModelFactory(private val repository: BloomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BloomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
