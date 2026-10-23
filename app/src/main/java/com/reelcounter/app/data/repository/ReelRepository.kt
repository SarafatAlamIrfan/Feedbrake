package com.reelcounter.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.reelcounter.app.data.model.DailyStats
import com.reelcounter.app.data.model.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class ReelRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("feedbrake_stats", Context.MODE_PRIVATE)

    private val _todayCount = MutableStateFlow(0)
    val todayCount: StateFlow<Int> = _todayCount.asStateFlow()

    private val _todayPlatformBreakdown = MutableStateFlow<Map<Platform, Int>>(emptyMap())
    val todayPlatformBreakdown: StateFlow<Map<Platform, Int>> = _todayPlatformBreakdown.asStateFlow()

    private val _todayStats = MutableStateFlow<DailyStats?>(null)
    val todayStats: StateFlow<DailyStats?> = _todayStats.asStateFlow()

    init {
        loadTodayData()
    }

    private fun getTodayKey(): String = LocalDate.now().toString()

    private fun loadTodayData() {
        val todayKey = getTodayKey()
        val count = prefs.getInt(todayKey + "_total", 0)
        _todayCount.value = count

        val breakdown = mutableMapOf<Platform, Int>()
        for (p in Platform.entries) {
            val pCount = prefs.getInt(todayKey + "_" + p.name, 0)
            if (pCount > 0) {
                breakdown[p] = pCount
            }
        }
        _todayPlatformBreakdown.value = breakdown
        updateStats(count, breakdown)
    }

    suspend fun recordReel(platform: Platform, durationSeconds: Int = 0) {
        val todayKey = getTodayKey()
        val newTotal = prefs.getInt(todayKey + "_total", 0) + 1
        val newPlatformCount = prefs.getInt(todayKey + "_" + platform.name, 0) + 1

        prefs.edit()
            .putInt(todayKey + "_total", newTotal)
            .putInt(todayKey + "_" + platform.name, newPlatformCount)
            .apply()

        _todayCount.value = newTotal

        val currentMap = _todayPlatformBreakdown.value.toMutableMap()
        currentMap[platform] = newPlatformCount
        _todayPlatformBreakdown.value = currentMap

        updateStats(newTotal, currentMap)
    }

    private fun updateStats(total: Int, breakdown: Map<Platform, Int>) {
        val estimatedSeconds = total * 25L
        val maxTarget = 50
        val zenScore = ((1f - (total.toFloat() / (maxTarget * 1.5f))).coerceIn(0f, 1f) * 100).toInt()

        _todayStats.value = DailyStats(
            dateEpochDay = LocalDate.now().toEpochDay(),
            totalCount = total,
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
                val repo = ReelRepository(context.applicationContext)
                INSTANCE = repo
                repo
            }
        }
    }
}
