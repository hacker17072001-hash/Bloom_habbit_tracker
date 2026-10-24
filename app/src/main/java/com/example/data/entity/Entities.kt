package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "check",
    val colorHex: String = "#6C5CE7",
    val categoryId: String = "Productivity",
    val type: String = "SIMPLE", // "SIMPLE", "QUANTITY", "DURATION"
    val targetValue: Double = 1.0,
    val unit: String = "times",
    val scheduleType: String = "EVERY_DAY", // "EVERY_DAY", "WEEKDAYS", "WEEKENDS", "SPECIFIC_DAYS"
    val scheduleDays: String = "1,2,3,4,5,6,7", // ISO days 1=Mon..7=Sun
    val preferredTime: String = "Anytime", // "Morning", "Afternoon", "Evening", "Anytime"
    val reminderTime: String = "",
    val notes: String = "",
    val startDate: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "localDate"], unique = true),
        Index(value = ["localDate"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val localDate: String, // YYYY-MM-DD
    val progressValue: Double = 0.0,
    val completionStatus: String = "PENDING", // "COMPLETED", "PARTIAL", "MISSED", "SKIPPED", "PENDING"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val localDate: String, // YYYY-MM-DD
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockCondition: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Float = 0f
)

@Entity(tableName = "daily_reflections")
data class DailyReflectionEntity(
    @PrimaryKey
    val localDate: String, // YYYY-MM-DD
    val whatWentWell: String = "",
    val whatWasDifficult: String = "",
    val whatToImprove: String = "",
    val mood: String = "GOOD", // "EXCELLENT", "GOOD", "NEUTRAL", "TIRED"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
