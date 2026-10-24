package com.example.data.repository

import com.example.data.database.BloomDatabase
import com.example.data.entity.AchievementEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.DailyReflectionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.entity.UserPreferenceEntity
import com.example.util.DateUtils
import com.example.util.HabitCalculations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BloomRepository(private val database: BloomDatabase) {

    private val habitDao = database.habitDao()
    private val logDao = database.habitLogDao()
    private val categoryDao = database.categoryDao()
    private val timerSessionDao = database.timerSessionDao()
    private val achievementDao = database.achievementDao()
    private val reflectionDao = database.dailyReflectionDao()
    private val preferenceDao = database.userPreferenceDao()

    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val archivedHabits: Flow<List<HabitEntity>> = habitDao.getArchivedHabits()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allAchievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val timerSessions: Flow<List<TimerSessionEntity>> = timerSessionDao.getAllSessions()
    val allLogs: Flow<List<HabitLogEntity>> = logDao.getAllLogs()

    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>> = logDao.getLogsForDate(date)

    fun getLogsBetweenDates(start: String, end: String): Flow<List<HabitLogEntity>> =
        logDao.getLogsBetweenDates(start, end)

    fun observeReflection(date: String): Flow<DailyReflectionEntity?> =
        reflectionDao.observeReflection(date)

    suspend fun toggleHabitCompletion(habit: HabitEntity, date: String) = withContext(Dispatchers.IO) {
        val existingLog = logDao.getLog(habit.id, date)
        val isCurrentlyCompleted = existingLog?.completionStatus == "COMPLETED" ||
                (habit.targetValue > 0 && (existingLog?.progressValue ?: 0.0) >= habit.targetValue)

        val updatedProgress = if (isCurrentlyCompleted) 0.0 else habit.targetValue
        val updatedStatus = if (isCurrentlyCompleted) "PENDING" else "COMPLETED"

        val log = (existingLog ?: HabitLogEntity(
            habitId = habit.id,
            localDate = date
        )).copy(
            progressValue = updatedProgress,
            completionStatus = updatedStatus,
            updatedAt = System.currentTimeMillis()
        )

        logDao.insertOrUpdate(log)
        evaluateAchievements()
    }

    suspend fun adjustHabitProgress(habit: HabitEntity, date: String, delta: Double) = withContext(Dispatchers.IO) {
        val existingLog = logDao.getLog(habit.id, date)
        val curVal = existingLog?.progressValue ?: 0.0
        val newVal = (curVal + delta).coerceAtLeast(0.0)

        val status = if (newVal >= habit.targetValue && habit.targetValue > 0) {
            "COMPLETED"
        } else if (newVal > 0) {
            "PARTIAL"
        } else {
            "PENDING"
        }

        val log = (existingLog ?: HabitLogEntity(
            habitId = habit.id,
            localDate = date
        )).copy(
            progressValue = newVal,
            completionStatus = status,
            updatedAt = System.currentTimeMillis()
        )

        logDao.insertOrUpdate(log)
        evaluateAchievements()
    }

    suspend fun setHabitProgressDirect(habit: HabitEntity, date: String, value: Double) = withContext(Dispatchers.IO) {
        val existingLog = logDao.getLog(habit.id, date)
        val newVal = value.coerceAtLeast(0.0)
        val status = if (newVal >= habit.targetValue && habit.targetValue > 0) {
            "COMPLETED"
        } else if (newVal > 0) {
            "PARTIAL"
        } else {
            "PENDING"
        }

        val log = (existingLog ?: HabitLogEntity(
            habitId = habit.id,
            localDate = date
        )).copy(
            progressValue = newVal,
            completionStatus = status,
            updatedAt = System.currentTimeMillis()
        )

        logDao.insertOrUpdate(log)
        evaluateAchievements()
    }

    suspend fun insertHabit(habit: HabitEntity): Long = withContext(Dispatchers.IO) {
        val id = habitDao.insert(habit)
        evaluateAchievements()
        id
    }

    suspend fun updateHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        habitDao.update(habit)
    }

    suspend fun setArchived(habitId: Long, archived: Boolean) = withContext(Dispatchers.IO) {
        val habit = habitDao.getHabitById(habitId) ?: return@withContext
        habitDao.update(habit.copy(isArchived = archived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteHabit(habitId: Long, deleteLogs: Boolean = false) = withContext(Dispatchers.IO) {
        habitDao.deleteById(habitId)
        if (deleteLogs) {
            logDao.deleteLogsForHabit(habitId)
        }
    }

    suspend fun addCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }

    suspend fun saveReflection(reflection: DailyReflectionEntity) = withContext(Dispatchers.IO) {
        reflectionDao.insertOrUpdate(reflection)
        evaluateAchievements()
    }

    suspend fun getReflection(date: String): DailyReflectionEntity? = withContext(Dispatchers.IO) {
        reflectionDao.getReflection(date)
    }

    suspend fun recordTimerSession(session: TimerSessionEntity) = withContext(Dispatchers.IO) {
        timerSessionDao.insert(session)
        // Also update habit progress if duration-based
        if (session.habitId > 0) {
            val habit = habitDao.getHabitById(session.habitId)
            if (habit != null && habit.type == "DURATION") {
                val minsAdded = session.durationSeconds / 60.0
                adjustHabitProgress(habit, session.localDate, minsAdded)
            }
        }
        evaluateAchievements()
    }

    suspend fun getPreference(key: String, default: String): String = withContext(Dispatchers.IO) {
        preferenceDao.getPreference(key) ?: default
    }

    suspend fun setPreference(key: String, value: String) = withContext(Dispatchers.IO) {
        preferenceDao.setPreference(UserPreferenceEntity(key, value))
    }

    suspend fun evaluateAchievements() = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllHabitsList()
        val logs = logDao.getAllLogsList()
        val today = DateUtils.today()
        val (overallStreak, _) = HabitCalculations.calculateOverallStreak(habits.filter { !it.isArchived }, logs, today)
        val completedLogsCount = logs.count { it.completionStatus == "COMPLETED" }
        val sessions = timerSessionDao.getAllSessionsList()
        val totalTimerMins = sessions.sumOf { it.durationSeconds } / 60
        val reflections = reflectionDao.getAllReflectionsList()

        val achievements = achievementDao.getAllAchievementsList().associateBy { it.id }.toMutableMap()

        // 1. First Step
        achievements["first_step"]?.let {
            if (!it.isUnlocked && completedLogsCount >= 1) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = (completedLogsCount.toFloat() / 1f).coerceIn(0f, 1f)))
            }
        }

        // 2. Streaks
        achievements["streak_3"]?.let {
            val prog = (overallStreak.toFloat() / 3f).coerceIn(0f, 1f)
            if (!it.isUnlocked && overallStreak >= 3) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        achievements["streak_7"]?.let {
            val prog = (overallStreak.toFloat() / 7f).coerceIn(0f, 1f)
            if (!it.isUnlocked && overallStreak >= 7) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        achievements["streak_14"]?.let {
            val prog = (overallStreak.toFloat() / 14f).coerceIn(0f, 1f)
            if (!it.isUnlocked && overallStreak >= 14) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        achievements["streak_30"]?.let {
            val prog = (overallStreak.toFloat() / 30f).coerceIn(0f, 1f)
            if (!it.isUnlocked && overallStreak >= 30) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        // 3. Century
        achievements["century"]?.let {
            val prog = (completedLogsCount.toFloat() / 100f).coerceIn(0f, 1f)
            if (!it.isUnlocked && completedLogsCount >= 100) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        // 4. Focus
        achievements["zen_master"]?.let {
            val prog = (totalTimerMins.toFloat() / 60f).coerceIn(0f, 1f)
            if (!it.isUnlocked && totalTimerMins >= 60) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            } else if (!it.isUnlocked) {
                achievementDao.update(it.copy(progress = prog))
            }
        }

        // 5. Mindful reflection
        achievements["mindful"]?.let {
            if (!it.isUnlocked && reflections.isNotEmpty()) {
                achievementDao.update(it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = 1f))
            }
        }
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val habitsArr = JSONArray()
        for (h in habitDao.getAllHabitsList()) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("description", h.description)
            obj.put("iconName", h.iconName)
            obj.put("colorHex", h.colorHex)
            obj.put("categoryId", h.categoryId)
            obj.put("type", h.type)
            obj.put("targetValue", h.targetValue)
            obj.put("unit", h.unit)
            obj.put("scheduleType", h.scheduleType)
            obj.put("scheduleDays", h.scheduleDays)
            obj.put("preferredTime", h.preferredTime)
            obj.put("startDate", h.startDate)
            obj.put("isArchived", h.isArchived)
            habitsArr.put(obj)
        }
        root.put("habits", habitsArr)

        val logsArr = JSONArray()
        for (l in logDao.getAllLogsList()) {
            val obj = JSONObject()
            obj.put("habitId", l.habitId)
            obj.put("localDate", l.localDate)
            obj.put("progressValue", l.progressValue)
            obj.put("completionStatus", l.completionStatus)
            obj.put("notes", l.notes)
            logsArr.put(obj)
        }
        root.put("logs", logsArr)

        val reflectionsArr = JSONArray()
        for (r in reflectionDao.getAllReflectionsList()) {
            val obj = JSONObject()
            obj.put("localDate", r.localDate)
            obj.put("whatWentWell", r.whatWentWell)
            obj.put("whatWasDifficult", r.whatWasDifficult)
            obj.put("whatToImprove", r.whatToImprove)
            obj.put("mood", r.mood)
            reflectionsArr.put(obj)
        }
        root.put("reflections", reflectionsArr)

        root.toString(2)
    }

    suspend fun importJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("habits")) return@withContext false

            val habitsArr = root.getJSONArray("habits")
            val newHabits = mutableListOf<HabitEntity>()
            for (i in 0 until habitsArr.length()) {
                val obj = habitsArr.getJSONObject(i)
                newHabits.add(
                    HabitEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        iconName = obj.optString("iconName", "check"),
                        colorHex = obj.optString("colorHex", "#6C5CE7"),
                        categoryId = obj.optString("categoryId", "Productivity"),
                        type = obj.optString("type", "SIMPLE"),
                        targetValue = obj.optDouble("targetValue", 1.0),
                        unit = obj.optString("unit", "times"),
                        scheduleType = obj.optString("scheduleType", "EVERY_DAY"),
                        scheduleDays = obj.optString("scheduleDays", "1,2,3,4,5,6,7"),
                        preferredTime = obj.optString("preferredTime", "Anytime"),
                        startDate = obj.optString("startDate", DateUtils.today()),
                        isArchived = obj.optBoolean("isArchived", false)
                    )
                )
            }
            if (newHabits.isNotEmpty()) {
                habitDao.insertAll(newHabits)
            }

            if (root.has("logs")) {
                val logsArr = root.getJSONArray("logs")
                val newLogs = mutableListOf<HabitLogEntity>()
                for (i in 0 until logsArr.length()) {
                    val obj = logsArr.getJSONObject(i)
                    newLogs.add(
                        HabitLogEntity(
                            habitId = obj.getLong("habitId"),
                            localDate = obj.getString("localDate"),
                            progressValue = obj.optDouble("progressValue", 1.0),
                            completionStatus = obj.optString("completionStatus", "COMPLETED"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (newLogs.isNotEmpty()) {
                    logDao.insertAll(newLogs)
                }
            }

            if (root.has("reflections")) {
                val reflArr = root.getJSONArray("reflections")
                val newRefls = mutableListOf<DailyReflectionEntity>()
                for (i in 0 until reflArr.length()) {
                    val obj = reflArr.getJSONObject(i)
                    newRefls.add(
                        DailyReflectionEntity(
                            localDate = obj.getString("localDate"),
                            whatWentWell = obj.optString("whatWentWell", ""),
                            whatWasDifficult = obj.optString("whatWasDifficult", ""),
                            whatToImprove = obj.optString("whatToImprove", ""),
                            mood = obj.optString("mood", "GOOD")
                        )
                    )
                }
                if (newRefls.isNotEmpty()) {
                    reflectionDao.insertAll(newRefls)
                }
            }

            evaluateAchievements()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun exportCsv(): String = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllHabitsList().associateBy { it.id }
        val logs = logDao.getAllLogsList()

        val sb = StringBuilder()
        sb.append("Date,Habit Name,Category,Type,Progress,Target,Unit,Status,Notes\n")
        for (l in logs) {
            val h = habits[l.habitId]
            val habitName = h?.name?.replace(",", " ") ?: "Unknown"
            val category = h?.categoryId ?: "General"
            val type = h?.type ?: "SIMPLE"
            val target = h?.targetValue ?: 1.0
            val unit = h?.unit ?: "times"
            sb.append("${l.localDate},\"$habitName\",$category,$type,${l.progressValue},$target,$unit,${l.completionStatus},\"${l.notes}\"\n")
        }
        sb.toString()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        habitDao.clearAll()
        logDao.clearAll()
        reflectionDao.clearAll()
        timerSessionDao.clearAll()
        achievementDao.clearAll()
        categoryDao.clearAll()
        preferenceDao.clearAll()
        BloomDatabase.seedDefaults(database)
    }
}
