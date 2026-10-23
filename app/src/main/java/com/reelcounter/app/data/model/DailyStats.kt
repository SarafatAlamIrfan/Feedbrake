package com.reelcounter.app.data.model

data class DailyStats(
    val dateEpochDay: Long,
    val totalCount: Int,
    val totalWatchTimeSeconds: Long,
    val platformCounts: Map<Platform, Int>,
    val zenScore: Int // 0 to 100 percentage
) {
    val totalWatchTimeMinutes: Int get() = (totalWatchTimeSeconds / 60).toInt()

    val dangerLevel: DangerLevel get() = when {
        totalCount < 20 -> DangerLevel.MINDFUL
        totalCount < 50 -> DangerLevel.MODERATE
        totalCount < 80 -> DangerLevel.WARNING
        else -> DangerLevel.DOOMSCROLL
    }
}

enum class DangerLevel(val label: String, val hexColor: Long) {
    MINDFUL("Mindful", 0xFF00E676),
    MODERATE("Moderate", 0xFF4FACFE),
    WARNING("Dopamine Spike", 0xFFFFB300),
    DOOMSCROLL("Doomscroll Alert!", 0xFFFF3366)
}
