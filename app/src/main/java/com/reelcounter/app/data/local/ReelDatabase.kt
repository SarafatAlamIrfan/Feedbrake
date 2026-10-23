package com.reelcounter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.model.ReelEvent

object PlatformTypeConverters {
    @TypeConverter
    fun fromPlatform(platform: Platform): String = platform.name

    @TypeConverter
    fun toPlatform(value: String): Platform = runCatching { Platform.valueOf(value) }.getOrDefault(Platform.OTHER)
}

@Database(entities = [ReelEvent::class], version = 1, exportSchema = false)
@TypeConverters(PlatformTypeConverters::class)
abstract class ReelDatabase : RoomDatabase() {
    abstract fun reelDao(): ReelDao

    companion object {
        @Volatile
        private var INSTANCE: ReelDatabase? = null

        fun getInstance(context: Context): ReelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReelDatabase::class.java,
                    "reel_counter.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
