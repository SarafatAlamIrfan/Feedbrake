package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

class GenericDetector : PlatformDetector {
    override val platform: Platform = Platform.OTHER

    override fun matchesPackage(packageName: String): Boolean = true

    override fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean {
        val className = event.className?.toString() ?: ""
        return className.contains("ViewPager2") || className.contains("VerticalSnap")
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
