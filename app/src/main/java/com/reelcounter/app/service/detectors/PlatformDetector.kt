package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

interface PlatformDetector {
    val platform: Platform
    fun matchesPackage(packageName: String): Boolean
    fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean
    fun isScrollGesture(event: AccessibilityEvent): Boolean
}
