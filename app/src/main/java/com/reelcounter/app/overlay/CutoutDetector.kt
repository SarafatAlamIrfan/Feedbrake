package com.reelcounter.app.overlay

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowManager

data class IslandAnchor(
    val hasCutout: Boolean,
    val centerX: Int,
    val topY: Int,
    val cutoutWidth: Int,
    val cutoutHeight: Int
)

object CutoutDetector {

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        if (result == 0) {
            val density = context.resources.displayMetrics.density
            result = (28 * density).toInt()
        }
        return result
    }

    fun hasCameraCutout(context: Context): Boolean {
        return detectCutout(context).hasCutout
    }

    fun detectCutout(context: Context): IslandAnchor {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val statusBarHeight = getStatusBarHeight(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                runCatching { windowManager.currentWindowMetrics.windowInsets }.getOrNull()
            } else {
                null
            }

            val cutout: DisplayCutout? = windowInsets?.displayCutout

            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                val rect: Rect = cutout.boundingRects.first()
                return IslandAnchor(
                    hasCutout = true,
                    centerX = rect.centerX(),
                    topY = rect.top.coerceAtLeast(8),
                    cutoutWidth = rect.width(),
                    cutoutHeight = rect.height()
                )
            }
        }

        return IslandAnchor(
            hasCutout = false,
            centerX = screenWidth / 2,
            topY = (statusBarHeight * 0.25f).toInt().coerceAtLeast(10),
            cutoutWidth = 90,
            cutoutHeight = 35
        )
    }
}
