package com.reelcounter.app.data.repository

import android.content.Context
import com.reelcounter.app.data.local.ReelDao
import com.reelcounter.app.data.local.ReelDatabase
import com.reelcounter.app.data.model.DailyStats
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.model.ReelEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class ReelRepository(private val dao: ReelDao) {

    private fun getStartOfDayMillis(): Long {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun getEndOfDayMillis(): Long {
        return LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    }

    suspend fun recordReel(platform: Platform, durationSeconds: Int = 0) {
        dao.insertEvent(
            ReelEvent(
                platform = platform,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds
            )
        )
    }

    val todayCount: Flow<Int> = dao.getCountBetween(getStartOfDayMillis(), getEndOfDayMillis())

    val todayPlatformBreakdown: Flow<Map<Platform, Int>> = dao.getPlatformCountsBetween(
        getStartOfDayMillis(),
        getEndOfDayMillis()
    ).map { list -> list.associate { it.platform to it.count } }

    val todayStats: Flow<DailyStats> = combine(
        todayCount,
        todayPlatformBreakdown
    ) { count, breakdown ->
        // Assume avg 25 seconds per reel if individual duration wasn't tracked
        val estimatedSeconds = count * 25L
        val maxTarget = 50
        val zenScore = ((1f - (count.toFloat() / (maxTarget * 1.5f))).coerceIn(0f, 1f) * 100).toInt()

        DailyStats(
            dateEpochDay = LocalDate.now().toEpochDay(),
            totalCount = count,
            totalWatchTimeSeconds = estimatedSeconds,
            platformCounts = breakdown,
            zenScore = zenScore
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ReelRepository? = null

        fun getInstance(context: Context): ReelRepository {
            return INSTANCE ?: synchronized(this) {
                val db = ReelDatabase.getInstance(context)
                val repo = ReelRepository(db.reelDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
