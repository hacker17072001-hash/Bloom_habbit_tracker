package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val displayFormat: SimpleDateFormat
        get() = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    private val fullDisplayFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private val monthYearFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun today(): String {
        return isoFormat.format(Date())
    }

    fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun parseParts(dateStr: String): Triple<Int, Int, Int> {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 1
            val d = parts[2].toIntOrNull() ?: 1
            return Triple(y, m, d)
        }
        val cal = Calendar.getInstance()
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * Returns 1 for Monday through 7 for Sunday (ISO-8601 standard)
     */
    fun getIsoDayOfWeek(dateStr: String): Int {
        val cal = Calendar.getInstance()
        try {
            val d = isoFormat.parse(dateStr)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        val calDay = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat
        return when (calDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun addDays(dateStr: String, daysToAdd: Int): String {
        val cal = Calendar.getInstance()
        try {
            val d = isoFormat.parse(dateStr)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return isoFormat.format(cal.time)
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeekForMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val calDay = cal.get(Calendar.DAY_OF_WEEK)
        return when (calDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun formatDisplay(dateStr: String): String {
        return try {
            val d = isoFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(d)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatFullDisplay(dateStr: String): String {
        return try {
            val d = isoFormat.parse(dateStr) ?: return dateStr
            fullDisplayFormat.format(d)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    fun isFutureDate(dateStr: String): Boolean {
        return dateStr > today()
    }

    fun getWeekdayName(isoDayOfWeek: Int): String {
        return when (isoDayOfWeek) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> ""
        }
    }
}
