package com.example.model

import com.example.data.entity.AchievementEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.DailyReflectionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity

enum class HabitType {
    SIMPLE,
    QUANTITY,
    DURATION
}

enum class ScheduleType {
    EVERY_DAY,
    WEEKDAYS,
    WEEKENDS,
    SPECIFIC_DAYS
}

enum class CompletionStatus {
    COMPLETED,
    PARTIAL,
    MISSED,
    SKIPPED,
    PENDING
}

data class HabitItem(
    val habit: HabitEntity,
    val todayLog: HabitLogEntity?,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isScheduledToday: Boolean = true
) {
    val progressValue: Double
        get() = todayLog?.progressValue ?: 0.0

    val isCompleted: Boolean
        get() = todayLog?.completionStatus == "COMPLETED" || (habit.targetValue > 0 && progressValue >= habit.targetValue)

    val progressRatio: Float
        get() = if (habit.targetValue > 0) {
            (progressValue / habit.targetValue).toFloat().coerceIn(0f, 1f)
        } else if (isCompleted) 1f else 0f
}

data class DayProgress(
    val date: String,
    val dayName: String,
    val dayNumber: Int,
    val scheduledCount: Int,
    val completedCount: Int,
    val percentage: Float,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class CategoryStat(
    val category: String,
    val colorHex: String,
    val completedCount: Int,
    val totalScheduled: Int,
    val percentage: Float
)

data class HabitRank(
    val habitId: Long,
    val habitName: String,
    val colorHex: String,
    val iconName: String,
    val completionRate: Float,
    val completedDays: Int,
    val totalScheduledDays: Int,
    val currentStreak: Int
)

data class AnalyticsState(
    val overallCompletionRate: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val dailyTrend: List<Pair<String, Float>> = emptyList(), // Date -> %
    val weeklyBars: List<Pair<String, Float>> = emptyList(), // Label -> %
    val categoryStats: List<CategoryStat> = emptyList(),
    val topHabits: List<HabitRank> = emptyList(),
    val attentionHabits: List<HabitRank> = emptyList(),
    val localInsights: List<String> = emptyList()
)

data class BloomDashboardState(
    val greeting: String = "Good Morning! 🌱",
    val motivationalQuote: String = "Every small step brings you closer to your goals.",
    val todayDate: String = "",
    val totalActiveHabits: Int = 0,
    val completedToday: Int = 0,
    val remainingToday: Int = 0,
    val todayPercentage: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyCompletionRate: Float = 0f,
    val monthlyCompletionRate: Float = 0f,
    val totalSuccessfulCompletions: Int = 0,
    val todayHabits: List<HabitItem> = emptyList(),
    val weekDays: List<DayProgress> = emptyList()
)
