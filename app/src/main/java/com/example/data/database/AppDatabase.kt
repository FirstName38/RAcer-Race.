package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.AlarmDao
import com.example.data.dao.AppUsageDao
import com.example.data.dao.ChatDao
import com.example.data.dao.FocusDao
import com.example.data.dao.HabitDao
import com.example.data.dao.JournalDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.SpecialDateDao
import com.example.data.dao.TaskDao
import com.example.data.dao.WellnessDao
import com.example.data.entity.AlarmEntity
import com.example.data.entity.AppUsageEntity
import com.example.data.entity.ChatMessageEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.JournalEntryEntity
import com.example.data.entity.SettingsEntity
import com.example.data.entity.SpecialDateEntity
import com.example.data.entity.TaskEntity
import com.example.data.entity.WellnessLogEntity
import com.example.data.entity.WellnessRoutineEntity
import com.example.data.model.FocusMode
import com.example.data.model.HabitFrequency
import com.example.data.model.JournalMood
import com.example.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromFocusMode(value: FocusMode): String = value.name

    @TypeConverter
    fun toFocusMode(value: String): FocusMode = try {
        FocusMode.valueOf(value)
    } catch (e: Exception) {
        FocusMode.POMODORO
    }

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = try {
        TaskPriority.valueOf(value)
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = try {
        HabitFrequency.valueOf(value)
    } catch (e: Exception) {
        HabitFrequency.DAILY
    }

    @TypeConverter
    fun fromJournalMood(value: JournalMood): String = value.name

    @TypeConverter
    fun toJournalMood(value: String): JournalMood = try {
        JournalMood.valueOf(value)
    } catch (e: Exception) {
        JournalMood.GOOD
    }
}

@Database(
    entities = [
        FocusSessionEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        JournalEntryEntity::class,
        AlarmEntity::class,
        AppUsageEntity::class,
        SettingsEntity::class,
        ChatMessageEntity::class,
        SpecialDateEntity::class,
        WellnessRoutineEntity::class,
        WellnessLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun journalDao(): JournalDao
    abstract fun alarmDao(): AlarmDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun settingsDao(): SettingsDao
    abstract fun chatDao(): ChatDao
    abstract fun specialDateDao(): SpecialDateDao
    abstract fun wellnessDao(): WellnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "racer_focus_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
