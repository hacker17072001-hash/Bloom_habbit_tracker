package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AchievementDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.DailyReflectionDao
import com.example.data.dao.HabitDao
import com.example.data.dao.HabitLogDao
import com.example.data.dao.TimerSessionDao
import com.example.data.dao.UserPreferenceDao
import com.example.data.entity.AchievementEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.DailyReflectionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.entity.UserPreferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        CategoryEntity::class,
        TimerSessionEntity::class,
        AchievementEntity::class,
        DailyReflectionEntity::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BloomDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun categoryDao(): CategoryDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: BloomDatabase? = null

        fun getDatabase(context: Context): BloomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloomDatabase::class.java,
                    "bloom_habits.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default categories and achievements in background
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDefaults(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaults(database: BloomDatabase) {
            val categoryDao = database.categoryDao()
            val existing = categoryDao.getAllCategoriesList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    CategoryEntity("Health & Fitness", "Health & Fitness", "fitness", "#36CFA0", true),
                    CategoryEntity("Learning", "Learning", "school", "#6C5CE7", true),
                    CategoryEntity("Productivity", "Productivity", "check", "#A29BFE", true),
                    CategoryEntity("Mental Wellness", "Mental Wellness", "spa", "#00CEC9", true),
                    CategoryEntity("Personal Growth", "Personal Growth", "sparkles", "#FFB86C", true),
                    CategoryEntity("Reading", "Reading", "book", "#FD79A8", true),
                    CategoryEntity("Finance", "Finance", "money", "#FDCB6E", true),
                    CategoryEntity("Lifestyle", "Lifestyle", "heart", "#FF7480", true)
                )
                categoryDao.insertAll(defaults)
            }

            val achievementDao = database.achievementDao()
            val existingAch = achievementDao.getAllAchievementsList()
            if (existingAch.isEmpty()) {
                val initialAchievements = listOf(
                    AchievementEntity("first_step", "First Blossom", "Complete your first habit", "sprout", "1 completed habit", false, null, 0f),
                    AchievementEntity("streak_3", "3-Day Momentum", "Maintain a 3-day active streak", "flame", "3-day streak", false, null, 0f),
                    AchievementEntity("streak_7", "Weekly Warrior", "Reach a 7-day consistency streak", "award", "7-day streak", false, null, 0f),
                    AchievementEntity("streak_14", "Fortnight Habit", "Maintain your streak for 14 straight days", "star", "14-day streak", false, null, 0f),
                    AchievementEntity("streak_30", "Habit Master", "Achieve a full 30-day streak milestone", "crown", "30-day streak", false, null, 0f),
                    AchievementEntity("century", "Century Club", "Log 100 successful habit completions", "trophy", "100 total completions", false, null, 0f),
                    AchievementEntity("perfect_day", "Flawless Day", "Complete 100% of all scheduled habits in a day", "sun", "100% daily completion", false, null, 0f),
                    AchievementEntity("zen_master", "Deep Focus", "Log at least 60 minutes in the Focus Timer", "timer", "60 mins timer focused", false, null, 0f),
                    AchievementEntity("mindful", "Mindful Reflect", "Complete your first daily reflection journal", "feather", "1 reflection saved", false, null, 0f)
                )
                achievementDao.insertAll(initialAchievements)
            }
        }
    }
}
