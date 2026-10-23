package com.reelcounter.app.service.engine

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.reelcounter.app.data.model.Platform

class ReelDebounceEngine(
    private val context: Context,
    private val minViewDurationMillis: Long = 1400L,
    private val onReelCounted: (platform: Platform, count: Int) -> Unit
) {
    private var lastScrollTime = 0L
    private var currentSessionCount = 0

    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun onPotentialReelSwipe(platform: Platform) {
        val now = System.currentTimeMillis()
        if (now - lastScrollTime >= minViewDurationMillis) {
            lastScrollTime = now
            currentSessionCount++
            triggerMicroHaptic()
            onReelCounted(platform, currentSessionCount)
        }
    }

    private fun triggerMicroHaptic() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (_: Exception) {}
    }
}
