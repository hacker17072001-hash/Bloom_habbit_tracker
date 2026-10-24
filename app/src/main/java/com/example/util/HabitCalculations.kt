package com.example.util

import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.model.CategoryStat
import com.example.model.HabitRank

object HabitCalculations {

    fun isHabitScheduledOn(habit: HabitEntity, dateStr: String): Boolean {
        if (dateStr < habit.startDate) return false
        val isoDay = DateUtils.getIsoDayOfWeek(dateStr) // 1=Mon .. 7=Sun
        return when (habit.scheduleType) {
            "EVERY_DAY" -> true
            "WEEKDAYS" -> isoDay in 1..5
            "WEEKENDS" -> isoDay in 6..7
            "SPECIFIC_DAYS" -> {
                val allowed = habit.scheduleDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                allowed.contains(isoDay)
            }
            else -> true
        }
    }

    /**
     * Calculates the active current streak for a single habit.
     * Respects scheduled days: non-scheduled rest days do NOT break streaks.
     * Today's unfinished status does not break streak from yesterday.
     */
    fun calculateHabitStreak(
        habit: HabitEntity,
        logsMap: Map<String, HabitLogEntity>, // Date -> Log
        todayDate: String = DateUtils.today()
    ): Pair<Int, Int> { // Pair(currentStreak, longestStreak)
        var currentDate = todayDate
        var currentStreak = 0
        var isFirstScheduledDay = true

        // Walk backwards up to 365 days
        for (i in 0..365) {
            val dateStr = DateUtils.addDays(todayDate, -i)
            if (dateStr < habit.startDate) break

            val isScheduled = isHabitScheduledOn(habit, dateStr)
            if (!isScheduled) {
                // Non-scheduled rest day: skip without breaking
                continue
            }

            val log = logsMap[dateStr]
            val isCompleted = log?.completionStatus == "COMPLETED" ||
                    (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
            val isSkipped = log?.completionStatus == "SKIPPED"

            if (dateStr == todayDate) {
                if (isCompleted) {
                    currentStreak++
                }
                // If not completed today, today is in-progress; streak from yesterday continues
            } else {
                if (isCompleted) {
                    currentStreak++
                } else if (isSkipped) {
                    // Excused skip does not break streak
                    continue
                } else {
                    // Missed a scheduled day in the past -> break active streak
                    break
                }
            }
        }

        // Calculate longest streak historically
        var longestStreak = currentStreak
        var runningStreak = 0
        val startDate = habit.startDate
        var iterDate = startDate

        while (iterDate <= todayDate) {
            val isScheduled = isHabitScheduledOn(habit, iterDate)
            if (isScheduled) {
                val log = logsMap[iterDate]
                val isCompleted = log?.completionStatus == "COMPLETED" ||
                        (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
                val isSkipped = log?.completionStatus == "SKIPPED"

                if (isCompleted) {
                    runningStreak++
                    if (runningStreak > longestStreak) {
                        longestStreak = runningStreak
                    }
                } else if (!isSkipped && iterDate != todayDate) {
                    runningStreak = 0
                }
            }
            iterDate = DateUtils.addDays(iterDate, 1)
        }

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Calculates overall daily streak across all habits.
     */
    fun calculateOverallStreak(
        activeHabits: List<HabitEntity>,
        allLogs: List<HabitLogEntity>,
        todayDate: String = DateUtils.today()
    ): Pair<Int, Int> {
        if (activeHabits.isEmpty()) return Pair(0, 0)

        val logsByDateAndHabit = allLogs.groupBy { it.localDate }
            .mapValues { entry -> entry.value.associateBy { it.habitId } }

        var currentStreak = 0
        for (i in 0..365) {
            val dateStr = DateUtils.addDays(todayDate, -i)
            val scheduledHabits = activeHabits.filter { isHabitScheduledOn(it, dateStr) }
            if (scheduledHabits.isEmpty()) continue

            val logsForDay = logsByDateAndHabit[dateStr] ?: emptyMap()
            val completedCount = scheduledHabits.count { habit ->
                val log = logsForDay[habit.id]
                log?.completionStatus == "COMPLETED" ||
                        (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
            }

            if (dateStr == todayDate) {
                if (completedCount > 0) {
                    currentStreak++
                }
            } else {
                if (completedCount > 0) {
                    currentStreak++
                } else {
                    break
                }
            }
        }

        // Longest overall streak
        var longestStreak = currentStreak
        var tempStreak = 0
        // Search earliest date
        val earliestDate = activeHabits.minOfOrNull { it.startDate } ?: todayDate
        var checkDate = earliestDate
        while (checkDate <= todayDate) {
            val scheduled = activeHabits.filter { isHabitScheduledOn(it, checkDate) }
            if (scheduled.isNotEmpty()) {
                val logsForDay = logsByDateAndHabit[checkDate] ?: emptyMap()
                val completed = scheduled.count { habit ->
                    val log = logsForDay[habit.id]
                    log?.completionStatus == "COMPLETED" ||
                            (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
                }
                if (completed > 0) {
                    tempStreak++
                    if (tempStreak > longestStreak) longestStreak = tempStreak
                } else if (checkDate != todayDate) {
                    tempStreak = 0
                }
            }
            checkDate = DateUtils.addDays(checkDate, 1)
        }

        return Pair(currentStreak, longestStreak)
    }

    fun calculateCategoryStats(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        startDate: String,
        endDate: String
    ): List<CategoryStat> {
        val habitsByCategory = habits.groupBy { it.categoryId }
        val logsByHabit = logs.groupBy { it.habitId }

        return habitsByCategory.map { (category, categoryHabits) ->
            var totalScheduled = 0
            var completedCount = 0
            var colorHex = categoryHabits.firstOrNull()?.colorHex ?: "#6C5CE7"

            for (habit in categoryHabits) {
                val habitLogs = logsByHabit[habit.id]?.associateBy { it.localDate } ?: emptyMap()
                var cur = startDate
                while (cur <= endDate) {
                    if (isHabitScheduledOn(habit, cur)) {
                        totalScheduled++
                        val log = habitLogs[cur]
                        if (log?.completionStatus == "COMPLETED" ||
                            (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
                        ) {
                            completedCount++
                        }
                    }
                    cur = DateUtils.addDays(cur, 1)
                }
            }

            val pct = if (totalScheduled > 0) (completedCount.toFloat() / totalScheduled) * 100f else 0f
            CategoryStat(
                category = category,
                colorHex = colorHex,
                completedCount = completedCount,
                totalScheduled = totalScheduled,
                percentage = pct
            )
        }.sortedByDescending { it.percentage }
    }

    fun calculateHabitRankings(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        startDate: String,
        endDate: String,
        todayDate: String = DateUtils.today()
    ): Pair<List<HabitRank>, List<HabitRank>> {
        val logsByHabit = logs.groupBy { it.habitId }

        val ranks = habits.map { habit ->
            val habitLogs = logsByHabit[habit.id]?.associateBy { it.localDate } ?: emptyMap()
            var totalScheduled = 0
            var completedCount = 0

            var cur = maxOf(startDate, habit.startDate)
            while (cur <= minOf(endDate, todayDate)) {
                if (isHabitScheduledOn(habit, cur)) {
                    totalScheduled++
                    val log = habitLogs[cur]
                    if (log?.completionStatus == "COMPLETED" ||
                        (habit.targetValue > 0 && (log?.progressValue ?: 0.0) >= habit.targetValue)
                    ) {
                        completedCount++
                    }
                }
                cur = DateUtils.addDays(cur, 1)
            }

            val (streak, _) = calculateHabitStreak(habit, habitLogs, todayDate)
            val rate = if (totalScheduled > 0) (completedCount.toFloat() / totalScheduled) * 100f else 0f

            HabitRank(
                habitId = habit.id,
                habitName = habit.name,
                colorHex = habit.colorHex,
                iconName = habit.iconName,
                completionRate = rate,
                completedDays = completedCount,
                totalScheduledDays = totalScheduled,
                currentStreak = streak
            )
        }

        val topHabits = ranks.filter { it.totalScheduledDays > 0 && it.completionRate >= 50f }
            .sortedByDescending { it.completionRate }

        val attentionHabits = ranks.filter { it.totalScheduledDays > 0 && it.completionRate < 50f }
            .sortedBy { it.completionRate }

        return Pair(topHabits, attentionHabits)
    }

    fun generateInsights(
        completionRate: Float,
        currentStreak: Int,
        topCategory: CategoryStat?,
        attentionHabit: HabitRank?
    ): List<String> {
        val insights = mutableListOf<String>()

        if (currentStreak >= 3) {
            insights.add("🔥 You have an active ${currentStreak}-day streak! Keep the flame alive.")
        } else {
            insights.add("🌱 Consistency begins with one small checkmark today.")
        }

        if (completionRate >= 80f) {
            insights.add("✨ Stellar progress! You completed ${completionRate.toInt()}% of scheduled habits.")
        } else if (completionRate >= 50f) {
            insights.add("📈 Good momentum with a ${completionRate.toInt()}% completion rate.")
        }

        if (topCategory != null && topCategory.totalScheduled > 0) {
            insights.add("🏆 ${topCategory.category} is your strongest area with ${topCategory.percentage.toInt()}% consistency.")
        }

        if (attentionHabit != null) {
            insights.add("💡 Giving '${attentionHabit.habitName}' a 5-minute head start today can help build its rhythm.")
        }

        return insights
    }
}
