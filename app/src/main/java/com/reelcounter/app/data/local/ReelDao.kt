package com.reelcounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.model.ReelEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {
    @Insert
    suspend fun insertEvent(event: ReelEvent): Long

    @Query("SELECT COUNT(*) FROM reel_events WHERE timestamp >= :startTimeMillis AND timestamp <= :endTimeMillis")
    fun getCountBetween(startTimeMillis: Long, endTimeMillis: Long): Flow<Int>

    @Query("SELECT * FROM reel_events WHERE timestamp >= :startTimeMillis AND timestamp <= :endTimeMillis ORDER BY timestamp DESC")
    fun getEventsBetween(startTimeMillis: Long, endTimeMillis: Long): Flow<List<ReelEvent>>

    @Query("SELECT platform, COUNT(*) as count FROM reel_events WHERE timestamp >= :startTimeMillis AND timestamp <= :endTimeMillis GROUP BY platform")
    fun getPlatformCountsBetween(startTimeMillis: Long, endTimeMillis: Long): Flow<List<PlatformCountResult>>

    @Query("SELECT COUNT(*) FROM reel_events")
    fun getTotalLifetimeCount(): Flow<Int>
}

data class PlatformCountResult(
    val platform: Platform,
    val count: Int
)
