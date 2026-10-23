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
        if (className.contains("ClipsViewer") || className.contains("ReelViewer") || className.contains("ModalActivity")) {
            return true
        }
        if (rootNode == null) return false
        val clipsNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container")
        val reelNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_root")
        val isClipsPresent = clipsNodes.isNotEmpty() || reelNodes.isNotEmpty()
        clipsNodes.forEach { it.recycle() }
        reelNodes.forEach { it.recycle() }
        return isClipsPresent
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return false
        return event.scrollDeltaY > 15 || event.scrollDeltaY < -15 || event.itemCount > 0
    }
}
