package com.reelcounter.app.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.overlay.DynamicIslandService
import com.reelcounter.app.service.detectors.*
import com.reelcounter.app.service.engine.ReelDebounceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReelAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: ReelRepository

    private val detectors: List<PlatformDetector> = listOf(
        InstagramDetector(),
        TikTokDetector(),
        YouTubeShortsDetector(),
        FacebookDetector(),
        GenericDetector()
    )

    private val debounceEngine by lazy {
        ReelDebounceEngine(this) { platform, sessionCount ->
            serviceScope.launch {
                repository.recordReel(platform)
            }
            DynamicIslandService.updateLiveCount(this, platform, sessionCount)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = ReelRepository.getInstance(this)
        Log.i("ReelAccessibility", "ReelAccessibilityService created and active")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        val matchedDetector = detectors.firstOrNull { it.matchesPackage(packageName) }

        if (matchedDetector != null) {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    val rootNode = rootInActiveWindow
                    val inReels = matchedDetector.isReelViewerActive(event, rootNode)
                    if (inReels) {
                        DynamicIslandService.showIsland(this, matchedDetector.platform)
                    }
                }

                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    // Scrolling in an active short video app triggers island & count
                    DynamicIslandService.showIsland(this, matchedDetector.platform)
                    if (matchedDetector.isScrollGesture(event)) {
                        debounceEngine.onPotentialReelSwipe(matchedDetector.platform)
                    }
                }
            }
        } else {
            // User switched away to another application (Launcher, Home, Settings, etc.)
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                DynamicIslandService.hideIsland(this)
            }
        }
    }

    override fun onInterrupt() {
        DynamicIslandService.hideIsland(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DynamicIslandService.hideIsland(this)
    }
}
