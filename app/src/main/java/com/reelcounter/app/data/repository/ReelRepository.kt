package com.reelcounter.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.reelcounter.app.data.model.DailyStats
import com.reelcounter.app.data.model.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

enum class DisplayMode(val label: String, val description: String) {
    AUTO("Smart Auto", "Uses punch-hole Island if available, otherwise Notification Bar"),
    DYNAMIC_ISLAND("Dynamic Island", "Pill anchored to camera punch-hole cutout"),
    NOTIFICATION_BAR("Notification Panel", "Live counter & progress bar in notification shade")
}

class ReelRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("feedbrake_stats", Context.MODE_PRIVATE)

    private val _todayCount = MutableStateFlow(0)
    val todayCount: StateFlow<Int> = _todayCount.asStateFlow()

    private val _todayPlatformBreakdown = MutableStateFlow<Map<Platform, Int>>(emptyMap())
    val todayPlatformBreakdown: StateFlow<Map<Platform, Int>> = _todayPlatformBreakdown.asStateFlow()

    private val _todayStats = MutableStateFlow<DailyStats?>(null)
    val todayStats: StateFlow<DailyStats?> = _todayStats.asStateFlow()

    private val _enabledPlatforms = MutableStateFlow<Set<Platform>>(emptySet())
    val enabledPlatforms: StateFlow<Set<Platform>> = _enabledPlatforms.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.AUTO)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    init {
        loadTodayData()
        loadEnabledPlatforms()
        loadDisplayMode()
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

    private fun loadEnabledPlatforms() {
        val enabled = mutableSetOf<Platform>()
        for (p in Platform.entries) {
            if (p == Platform.OTHER) continue
            val defaultEnabled = when (p) {
                Platform.INSTAGRAM, Platform.TIKTOK, Platform.YOUTUBE_SHORTS, Platform.FACEBOOK -> true
                else -> false
            }
            if (prefs.getBoolean("track_platform_" + p.name, defaultEnabled)) {
                enabled.add(p)
            }
        }
        _enabledPlatforms.value = enabled
    }

    fun isPlatformEnabled(platform: Platform): Boolean {
        if (platform == Platform.OTHER) return false
        val defaultEnabled = when (platform) {
            Platform.INSTAGRAM, Platform.TIKTOK, Platform.YOUTUBE_SHORTS, Platform.FACEBOOK -> true
            else -> false
        }
        return prefs.getBoolean("track_platform_" + platform.name, defaultEnabled)
    }

    fun setPlatformEnabled(platform: Platform, enabled: Boolean) {
        prefs.edit().putBoolean("track_platform_" + platform.name, enabled).apply()
        loadEnabledPlatforms()
    }

    private fun loadDisplayMode() {
        val modeStr = prefs.getString("display_mode", DisplayMode.AUTO.name) ?: DisplayMode.AUTO.name
        _displayMode.value = runCatching { DisplayMode.valueOf(modeStr) }.getOrDefault(DisplayMode.AUTO)
    }

    fun setDisplayMode(mode: DisplayMode) {
        prefs.edit().putString("display_mode", mode.name).apply()
        _displayMode.value = mode
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
