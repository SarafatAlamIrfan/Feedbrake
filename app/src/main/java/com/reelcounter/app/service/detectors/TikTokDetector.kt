package com.reelcounter.app.service.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelcounter.app.data.model.Platform

class TikTokDetector : PlatformDetector {
    override val platform: Platform = Platform.TIKTOK

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.zhiliaoapp.musically" || packageName == "com.ss.android.ugc.trill"
    }

    override fun isReelViewerActive(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): Boolean {
        return true
    }

    override fun isScrollGesture(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
