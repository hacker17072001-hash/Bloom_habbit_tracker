package com.example

import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.util.DateUtils
import com.example.util.HabitCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitLogicTest {

    @Test
    fun testDateUtilsFormattingAndMath() {
        val date = "2026-10-23"
        val (y, m, d) = DateUtils.parseParts(date)
        assertEquals(2026, y)
        assertEquals(10, m)
        assertEquals(23, d)

        // Month boundary addition
        val nextMonth = DateUtils.addDays("2026-10-31", 1)
        assertEquals("2026-11-01", nextMonth)

        val leapDay = DateUtils.getDaysInMonth(2024, 2)
        assertEquals(29, leapDay)

        val nonLeapDay = DateUtils.getDaysInMonth(2025, 2)
        assertEquals(28, nonLeapDay)
    }

    @Test
    fun testScheduleTypeEvaluation() {
        val everydayHabit = HabitEntity(
            id = 1,
            name = "Drink Water",
            startDate = "2026-10-01",
            scheduleType = "EVERY_DAY"
        )
        assertTrue(HabitCalculations.isHabitScheduledOn(everydayHabit, "2026-10-23"))

        // Friday 2026-10-23: isoDay = 5
        val weekdayHabit = HabitEntity(
            id = 2,
            name = "Workout",
            startDate = "2026-10-01",
            scheduleType = "WEEKDAYS"
        )
        assertTrue(HabitCalculations.isHabitScheduledOn(weekdayHabit, "2026-10-23")) // Friday
        assertFalse(HabitCalculations.isHabitScheduledOn(weekdayHabit, "2026-10-24")) // Saturday
        assertFalse(HabitCalculations.isHabitScheduledOn(weekdayHabit, "2026-10-25")) // Sunday

        // Weekend habit
        val weekendHabit = HabitEntity(
            id = 3,
            name = "Hike",
            startDate = "2026-10-01",
            scheduleType = "WEEKENDS"
        )
        assertFalse(HabitCalculations.isHabitScheduledOn(weekendHabit, "2026-10-23"))
        assertTrue(HabitCalculations.isHabitScheduledOn(weekendHabit, "2026-10-24"))
    }

    @Test
    fun testStreakCalculationsWithConsecutiveDays() {
        val habit = HabitEntity(
            id = 1,
            name = "Meditation",
            startDate = "2026-10-20",
            scheduleType = "EVERY_DAY",
            targetValue = 1.0
        )

        val logs = mapOf(
            "2026-10-20" to HabitLogEntity(habitId = 1, localDate = "2026-10-20", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-21" to HabitLogEntity(habitId = 1, localDate = "2026-10-21", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-22" to HabitLogEntity(habitId = 1, localDate = "2026-10-22", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-23" to HabitLogEntity(habitId = 1, localDate = "2026-10-23", completionStatus = "COMPLETED", progressValue = 1.0)
        )

        val (curStreak, longStreak) = HabitCalculations.calculateHabitStreak(habit, logs, "2026-10-23")
        assertEquals(4, curStreak)
        assertEquals(4, longStreak)
    }

    @Test
    fun testStreakPreservedWhenTodayInProgress() {
        val habit = HabitEntity(
            id = 1,
            name = "Reading",
            startDate = "2026-10-20",
            scheduleType = "EVERY_DAY",
            targetValue = 20.0
        )

        // Completed 20, 21, 22; today 23 is still in progress (pending)
        val logs = mapOf(
            "2026-10-20" to HabitLogEntity(habitId = 1, localDate = "2026-10-20", completionStatus = "COMPLETED", progressValue = 20.0),
            "2026-10-21" to HabitLogEntity(habitId = 1, localDate = "2026-10-21", completionStatus = "COMPLETED", progressValue = 20.0),
            "2026-10-22" to HabitLogEntity(habitId = 1, localDate = "2026-10-22", completionStatus = "COMPLETED", progressValue = 20.0)
        )

        val (curStreak, _) = HabitCalculations.calculateHabitStreak(habit, logs, "2026-10-23")
        // Today is not completed yet, but streak from yesterday is kept active!
        assertEquals(3, curStreak)
    }

    @Test
    fun testStreakBreaksOnPastMissedDay() {
        val habit = HabitEntity(
            id = 1,
            name = "Reading",
            startDate = "2026-10-20",
            scheduleType = "EVERY_DAY",
            targetValue = 1.0
        )

        // Completed 20, missed 21, completed 22, completed 23
        val logs = mapOf(
            "2026-10-20" to HabitLogEntity(habitId = 1, localDate = "2026-10-20", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-22" to HabitLogEntity(habitId = 1, localDate = "2026-10-22", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-23" to HabitLogEntity(habitId = 1, localDate = "2026-10-23", completionStatus = "COMPLETED", progressValue = 1.0)
        )

        val (curStreak, _) = HabitCalculations.calculateHabitStreak(habit, logs, "2026-10-23")
        assertEquals(2, curStreak) // 22 and 23
    }

    @Test
    fun testWeekdayHabitStreakIgnoresWeekends() {
        val habit = HabitEntity(
            id = 1,
            name = "Work Focus",
            startDate = "2026-10-15",
            scheduleType = "WEEKDAYS",
            targetValue = 1.0
        )

        // Friday 2026-10-16 completed, Saturday 17 (rest), Sunday 18 (rest), Monday 19 completed
        val logs = mapOf(
            "2026-10-16" to HabitLogEntity(habitId = 1, localDate = "2026-10-16", completionStatus = "COMPLETED", progressValue = 1.0),
            "2026-10-19" to HabitLogEntity(habitId = 1, localDate = "2026-10-19", completionStatus = "COMPLETED", progressValue = 1.0)
        )

        val (curStreak, _) = HabitCalculations.calculateHabitStreak(habit, logs, "2026-10-19")
        // Weekend rest days did not break streak
        assertEquals(2, curStreak)
    }
}
