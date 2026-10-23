package com.reelcounter.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reel_events")
data class ReelEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: Platform,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
