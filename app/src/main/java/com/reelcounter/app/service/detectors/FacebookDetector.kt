package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

class FacebookDetector : PlatformDetector {
    override val platform: Platform = Platform.FACEBOOK

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.facebook.katana" || packageName == "com.facebook.lite"
    }

    override fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean {
        val className = event.className?.toString() ?: ""
        if (className.contains("FbReels") || className.contains("ShortFormVideo")) {
            return true
        }

        if (rootNode == null) return false

        val fbReelNodes = rootNode.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/fb_shorts_container")
        val reelVideoNodes = rootNode.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/reel_viewer_container")

        val isReel = fbReelNodes.isNotEmpty() || reelVideoNodes.isNotEmpty()

        fbReelNodes.forEach { it.recycle() }
        reelVideoNodes.forEach { it.recycle() }

        return isReel
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
