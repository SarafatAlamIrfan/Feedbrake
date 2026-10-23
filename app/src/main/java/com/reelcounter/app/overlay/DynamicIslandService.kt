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
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.reelcounter.app.data.model.Platform
import kotlinx.coroutines.flow.MutableStateFlow

class DynamicIslandService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var isIslandVisible = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundNotification()
    }

    private fun startForegroundNotification() {
        val channelId = "feedbrake_island_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dynamic Island Active Tracker",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Feedbrake Live")
            .setContentText("Dynamic Island tracking active reels")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_SHOW -> {
                val platformName = intent.getStringExtra(EXTRA_PLATFORM) ?: Platform.INSTAGRAM.name
                val platform = runCatching { Platform.valueOf(platformName) }.getOrDefault(Platform.INSTAGRAM)
                currentPlatformState.value = platform
                showIslandOverlay()
            }
            ACTION_HIDE -> {
                hideIslandOverlay()
            }
            ACTION_UPDATE_COUNT -> {
                val count = intent.getIntExtra(EXTRA_COUNT, 0)
                liveCountState.value = count
            }
        }
        return START_STICKY
    }

    private fun showIslandOverlay() {
        if (isIslandVisible) return
        val anchor = CutoutDetector.detectCutout(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = if (anchor.hasCutout) anchor.topY else 24
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        composeView = ComposeView(this).apply {
            ViewTreeLifecycleOwner.set(this, this@DynamicIslandService)
            ViewTreeSavedStateRegistryOwner.set(this, this@DynamicIslandService)
            ViewTreeViewModelStoreOwner.set(this, this@DynamicIslandService)
            setContent {
                DynamicIslandOverlay(
                    platformState = currentPlatformState,
                    countState = liveCountState,
                    onDismiss = { hideIslandOverlay() }
                )
            }
        }

        windowManager?.addView(composeView, params)
        isIslandVisible = true
    }

    private fun hideIslandOverlay() {
        if (!isIslandVisible) return
        composeView?.let {
            windowManager?.removeView(it)
        }
        composeView = null
        isIslandVisible = false
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

    companion object {
        private const val ACTION_SHOW = "com.reelcounter.app.SHOW_ISLAND"
        private const val ACTION_HIDE = "com.reelcounter.app.HIDE_ISLAND"
        private const val ACTION_UPDATE_COUNT = "com.reelcounter.app.UPDATE_COUNT"
        private const val EXTRA_PLATFORM = "extra_platform"
        private const val EXTRA_COUNT = "extra_count"

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
}
