package com.reelcounter.app.data.model

data class ReelEvent(
    val id: Long = 0,
    val platform: Platform,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
