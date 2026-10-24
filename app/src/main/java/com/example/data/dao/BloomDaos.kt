package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AchievementEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.DailyReflectionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.entity.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllHabitsList(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits")
    suspend fun clearAll()
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE localDate = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE localDate = :date")
    suspend fun getLogsForDateList(date: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND localDate = :date")
    suspend fun getLog(habitId: Long, date: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY localDate ASC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY localDate ASC")
    suspend fun getLogsForHabitList(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE localDate BETWEEN :startDate AND :endDate ORDER BY localDate ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE localDate BETWEEN :startDate AND :endDate ORDER BY localDate ASC")
    suspend fun getLogsBetweenDatesList(startDate: String, endDate: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs ORDER BY localDate ASC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY localDate ASC")
    suspend fun getAllLogsList(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: HabitLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun clearAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getSessionsForHabit(habitId: Long): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions ORDER BY timestamp DESC")
    suspend fun getAllSessionsList(): List<TimerSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TimerSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<TimerSessionEntity>)

    @Query("DELETE FROM timer_sessions")
    suspend fun clearAll()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    suspend fun getAllAchievementsList(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE localDate = :date")
    suspend fun getReflection(date: String): DailyReflectionEntity?

    @Query("SELECT * FROM daily_reflections WHERE localDate = :date")
    fun observeReflection(date: String): Flow<DailyReflectionEntity?>

    @Query("SELECT * FROM daily_reflections ORDER BY localDate DESC")
    fun getAllReflections(): Flow<List<DailyReflectionEntity>>

    @Query("SELECT * FROM daily_reflections ORDER BY localDate DESC")
    suspend fun getAllReflectionsList(): List<DailyReflectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(reflection: DailyReflectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reflections: List<DailyReflectionEntity>)

    @Query("DELETE FROM daily_reflections")
    suspend fun clearAll()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): String?

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllPreferences(): List<UserPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: UserPreferenceEntity)

    @Query("DELETE FROM user_preferences")
    suspend fun clearAll()
}
