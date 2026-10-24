package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

class InstagramDetector : PlatformDetector {
    override val platform: Platform = Platform.INSTAGRAM

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.instagram.android"
    }

    override fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean {
        val className = event.className?.toString() ?: ""
        // Instagram reels viewer class or activity
        if (className.contains("ClipsViewer") || className.contains("ReelViewerFragment")) {
            return true
        }

        if (rootNode == null) return false

        // Look specifically for reel/clip video layout container IDs
        val clipsNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container")
        val reelRootNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_root")
        val clipsSwipeNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_swipe_refresh_container")

        val isReel = clipsNodes.isNotEmpty() || reelRootNodes.isNotEmpty() || clipsSwipeNodes.isNotEmpty()

        clipsNodes.forEach { it.recycle() }
        reelRootNodes.forEach { it.recycle() }
        clipsSwipeNodes.forEach { it.recycle() }

        return isReel
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
