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

    // Only strictly supported short-form video platforms (NO generic wildcard!)
    private val detectors: List<PlatformDetector> = listOf(
        InstagramDetector(),
        TikTokDetector(),
        YouTubeShortsDetector(),
        FacebookDetector()
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
        Log.i("ReelAccessibility", "ReelAccessibilityService initialized")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        // Check if the current foreground app matches any supported short video platform
        val matchedDetector = detectors.firstOrNull { it.matchesPackage(packageName) }

        if (matchedDetector != null) {
            // Check if the user has enabled tracking for this specific platform
            if (!repository.isPlatformEnabled(matchedDetector.platform)) {
                DynamicIslandService.hideIsland(this)
                return
            }

            val rootNode = rootInActiveWindow
            val inReels = matchedDetector.isReelViewerActive(event, rootNode)

            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    if (inReels) {
                        DynamicIslandService.showIsland(this, matchedDetector.platform)
                    } else {
                        // User is in Instagram/YouTube but browsing home feed, profile, or search -> hide island!
                        DynamicIslandService.hideIsland(this)
                    }
                }

                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    // Only count swipe if user is ACTUALLY inside the reel viewer
                    if (inReels && matchedDetector.isScrollGesture(event)) {
                        DynamicIslandService.showIsland(this, matchedDetector.platform)
                        debounceEngine.onPotentialReelSwipe(matchedDetector.platform)
                    }
                }
            }
        } else {
            // User switched to Home screen, Launcher, Gallery, or any other non-video app -> ALWAYS HIDE ISLAND!
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
