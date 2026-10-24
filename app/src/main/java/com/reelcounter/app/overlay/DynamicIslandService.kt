package com.reelcounter.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.repository.DisplayMode
import com.reelcounter.app.data.repository.ReelRepository
import kotlinx.coroutines.flow.MutableStateFlow

class DynamicIslandService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)
    private val store: ViewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var isIslandVisible = false

    private val hideHandler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable {
        hideIslandOverlay()
    }

    companion object {
        private const val TAG = "DynamicIslandService"
        private const val ACTION_SHOW = "com.reelcounter.app.SHOW_ISLAND"
        private const val ACTION_HIDE = "com.reelcounter.app.HIDE_ISLAND"
        private const val ACTION_UPDATE_COUNT = "com.reelcounter.app.UPDATE_COUNT"
        private const val EXTRA_PLATFORM = "extra_platform"
        private const val EXTRA_COUNT = "extra_count"

        private const val CHANNEL_ID = "feedbrake_live_tracker"
        private const val NOTIFICATION_ID = 2001

        val currentPlatformState = MutableStateFlow(Platform.INSTAGRAM)
        val liveCountState = MutableStateFlow(0)

        fun showIsland(context: Context, platform: Platform) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_PLATFORM, platform.name)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start DynamicIslandService: ${e.message}")
            }
        }

        fun hideIsland(context: Context) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_HIDE
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hide DynamicIsland: ${e.message}")
            }
        }

        fun updateLiveCount(context: Context, platform: Platform, count: Int) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_UPDATE_COUNT
                putExtra(EXTRA_PLATFORM, platform.name)
                putExtra(EXTRA_COUNT, count)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update live count: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        try {
            val notification = createNotification(currentPlatformState.value, liveCountState.value)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startForeground non-fatal error: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val platformName = intent.getStringExtra(EXTRA_PLATFORM) ?: Platform.INSTAGRAM.name
                val p = runCatching { Platform.valueOf(platformName) }.getOrDefault(Platform.INSTAGRAM)
                currentPlatformState.value = p
                handleDisplayTrigger(p, liveCountState.value)
            }
            ACTION_UPDATE_COUNT -> {
                val count = intent.getIntExtra(EXTRA_COUNT, liveCountState.value + 1)
                liveCountState.value = count
                handleDisplayTrigger(currentPlatformState.value, count)
            }
            ACTION_HIDE -> {
                hideIslandOverlay()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun handleDisplayTrigger(platform: Platform, count: Int) {
        val repo = ReelRepository.getInstance(this)
        val mode = repo.displayMode.value
        val hasCutout = CutoutDetector.hasCameraCutout(this)

        // Always keep the notification updated with the live reel progress
        updateNotification(platform, count)

        // Decide whether to show the overlay pill
        val shouldShowPill = when (mode) {
            DisplayMode.DYNAMIC_ISLAND -> true
            DisplayMode.NOTIFICATION_BAR -> false
            DisplayMode.AUTO -> hasCutout // Only if phone has cutout (e.g. punch-hole / iQOO / Vivo Origin Island)
        }

        if (shouldShowPill) {
            showIslandOverlay()
            resetAutoHideTimer()
        } else {
            hideIslandOverlay()
        }
    }

    private fun resetAutoHideTimer() {
        hideHandler.removeCallbacks(autoDismissRunnable)
        // Auto-dismiss island after 5 seconds of inactivity so it NEVER stays stuck
        hideHandler.postDelayed(autoDismissRunnable, 5000L)
    }

    private fun showIslandOverlay() {
        if (isIslandVisible) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Overlay permission not granted")
            return
        }

        val anchor = CutoutDetector.detectCutout(this)
        val density = resources.displayMetrics.density

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = if (anchor.hasCutout && anchor.topY > 0) {
                anchor.topY
            } else {
                (8 * density).toInt().coerceAtLeast(16)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@DynamicIslandService)
            setViewTreeSavedStateRegistryOwner(this@DynamicIslandService)
            setViewTreeViewModelStoreOwner(this@DynamicIslandService)
            setContent {
                DynamicIslandOverlay(
                    platformState = currentPlatformState,
                    countState = liveCountState,
                    onDismiss = { hideIslandOverlay() }
                )
            }
        }

        try {
            windowManager?.addView(composeView, params)
            isIslandVisible = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add island view: ${e.message}")
        }
    }

    private fun hideIslandOverlay() {
        hideHandler.removeCallbacks(autoDismissRunnable)
        if (!isIslandVisible) return
        try {
            composeView?.let { windowManager?.removeView(it) }
            composeView = null
            isIslandVisible = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove island view: ${e.message}")
        }
    }

    private fun updateNotification(platform: Platform, count: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, createNotification(platform, count))
    }

    private fun createNotification(platform: Platform, count: Int): Notification {
        val channelName = "Feedbrake Live Reel Counter"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live reel intake count and progress bar"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val targetLimit = 50
        val estimatedMins = (count * 25) / 60
        val statusText = when {
            count < 20 -> "Mindful Focus"
            count < 50 -> "Moderate Intake"
            else -> "Doomscroll Alert!"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${platform.iconEmoji} ${platform.displayName} • $count Reels Watched")
            .setContentText("$statusText • ~$estimatedMins mins spent")
            .setProgress(targetLimit, count.coerceAtMost(targetLimit), false)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        hideIslandOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
