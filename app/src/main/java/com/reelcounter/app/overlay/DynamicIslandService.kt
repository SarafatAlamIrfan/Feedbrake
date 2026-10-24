package com.reelcounter.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
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

    companion object {
        private const val ACTION_SHOW = "com.reelcounter.app.SHOW_ISLAND"
        private const val ACTION_HIDE = "com.reelcounter.app.HIDE_ISLAND"
        private const val ACTION_UPDATE_COUNT = "com.reelcounter.app.UPDATE_COUNT"
        private const val EXTRA_PLATFORM = "extra_platform"
        private const val EXTRA_COUNT = "extra_count"

        private const val CHANNEL_ID = "feedbrake_dynamic_island"
        private const val NOTIFICATION_ID = 2001

        val currentPlatformState = MutableStateFlow(Platform.INSTAGRAM)
        val liveCountState = MutableStateFlow(0)

        fun showIsland(context: Context, platform: Platform) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_PLATFORM, platform.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun hideIsland(context: Context) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }

        fun updateLiveCount(context: Context, platform: Platform, count: Int) {
            val intent = Intent(context, DynamicIslandService::class.java).apply {
                action = ACTION_UPDATE_COUNT
                putExtra(EXTRA_PLATFORM, platform.name)
                putExtra(EXTRA_COUNT, count)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val platformName = intent.getStringExtra(EXTRA_PLATFORM) ?: Platform.INSTAGRAM.name
                currentPlatformState.value = runCatching { Platform.valueOf(platformName) }.getOrDefault(Platform.OTHER)
                showIslandOverlay()
            }
            ACTION_UPDATE_COUNT -> {
                val count = intent.getIntExtra(EXTRA_COUNT, liveCountState.value + 1)
                liveCountState.value = count
            }
            ACTION_HIDE -> {
                hideIslandOverlay()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun showIslandOverlay() {
        if (isIslandVisible) return
        val anchor = CutoutDetector.detectCutout(this)

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
            y = if (anchor.hasCutout) anchor.topY else 24
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
            e.printStackTrace()
        }
    }

    private fun hideIslandOverlay() {
        if (!isIslandVisible) return
        try {
            composeView?.let { windowManager?.removeView(it) }
            composeView = null
            isIslandVisible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(): Notification {
        val channelName = "Feedbrake Dynamic Island"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Shows live dynamic island for active reel counter"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Feedbrake Active")
            .setContentText("Dynamic Island tracking reel scrolls")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
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
