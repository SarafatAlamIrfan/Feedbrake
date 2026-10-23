package com.reelcounter.app.service

import android.accessibilityservice.AccessibilityService
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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        val matchedDetector = detectors.firstOrNull { it.matchesPackage(packageName) } ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val rootNode = rootInActiveWindow
                val inReels = matchedDetector.isReelViewerActive(event, rootNode)
                if (inReels) {
                    DynamicIslandService.showIsland(this, matchedDetector.platform)
                } else {
                    DynamicIslandService.hideIsland(this)
                }
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (matchedDetector.isScrollGesture(event)) {
                    debounceEngine.onPotentialReelSwipe(matchedDetector.platform)
                }
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
