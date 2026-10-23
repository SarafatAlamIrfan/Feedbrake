package com.reelcounter.app.overlay

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowInsets
import android.view.WindowManager

data class IslandAnchor(
    val hasCutout: Boolean,
    val centerX: Int,
    val topY: Int,
    val cutoutWidth: Int,
    val cutoutHeight: Int
)

object CutoutDetector {

    fun detectCutout(context: Context): IslandAnchor {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.windowInsets
            } else {
                null
            }

            val cutout: DisplayCutout? = windowInsets?.displayCutout

            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                val rect: Rect = cutout.boundingRects.first()
                return IslandAnchor(
                    hasCutout = true,
                    centerX = rect.centerX(),
                    topY = rect.top,
                    cutoutWidth = rect.width(),
                    cutoutHeight = rect.height()
                )
            }
        }

        return IslandAnchor(
            hasCutout = false,
            centerX = screenWidth / 2,
            topY = 12,
            cutoutWidth = 90,
            cutoutHeight = 35
        )
    }
}
