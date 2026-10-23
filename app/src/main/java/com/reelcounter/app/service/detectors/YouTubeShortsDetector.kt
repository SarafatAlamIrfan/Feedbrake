package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

class YouTubeShortsDetector : PlatformDetector {
    override val platform: Platform = Platform.YOUTUBE_SHORTS

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.google.android.youtube"
    }

    override fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean {
        val className = event.className?.toString() ?: ""
        if (className.contains("ShortsPlayer") || className.contains("ReelWatchFragment")) {
            return true
        }
        if (rootNode == null) return false
        val shortsNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
        val playerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/shorts_container")
        val isShorts = shortsNodes.isNotEmpty() || playerNodes.isNotEmpty()
        shortsNodes.forEach { it.recycle() }
        playerNodes.forEach { it.recycle() }
        return isShorts
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
