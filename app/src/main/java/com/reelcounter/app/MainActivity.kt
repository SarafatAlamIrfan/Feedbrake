package com.reelcounter.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.repository.DisplayMode
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.overlay.CutoutDetector
import com.reelcounter.app.overlay.DynamicIslandService
import com.reelcounter.app.service.ReelAccessibilityService
import com.reelcounter.app.ui.screens.DashboardScreen
import com.reelcounter.app.ui.theme.ReelCounterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: ReelRepository
    private var isAccessibilityEnabledState = mutableStateOf(false)
    private var isOverlayGrantedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ReelRepository.getInstance(this)

        setContent {
            ReelCounterTheme {
                val stats by repository.todayStats.collectAsState(initial = null)
                val enabledPlatforms by repository.enabledPlatforms.collectAsState()
                val displayMode by repository.displayMode.collectAsState()
                val isAccessibility by isAccessibilityEnabledState
                val isOverlay by isOverlayGrantedState

                val hasCutout = remember { CutoutDetector.hasCameraCutout(this@MainActivity) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        stats = stats,
                        isAccessibilityEnabled = isAccessibility,
                        isOverlayGranted = isOverlay,
                        enabledPlatforms = enabledPlatforms,
                        currentDisplayMode = displayMode,
                        hasCameraCutout = hasCutout,
                        onTogglePlatform = { platform, isEnabled ->
                            repository.setPlatformEnabled(platform, isEnabled)
                        },
                        onChangeDisplayMode = { mode ->
                            repository.setDisplayMode(mode)
                        },
                        onOpenAccessibilitySettings = { openAccessibilitySettings() },
                        onOpenOverlaySettings = { openOverlaySettings() },
                        onTestDynamicIsland = { testDynamicIsland() },
                        onDismissDynamicIsland = { dismissDynamicIsland() },
                        onSimulateReelSwipe = { simulateReelSwipe() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        isAccessibilityEnabledState.value = isAccessibilityServiceEnabled(this, ReelAccessibilityService::class.java)
        isOverlayGrantedState.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun testDynamicIsland() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant Display Over Other Apps first", Toast.LENGTH_LONG).show()
            openOverlaySettings()
            return
        }
        val currentCount = repository.todayCount.value
        DynamicIslandService.showIsland(this, Platform.INSTAGRAM)
        DynamicIslandService.updateLiveCount(this, Platform.INSTAGRAM, currentCount + 1)
        Toast.makeText(this, "⚡ Dynamic Island activated! Tap it to expand.", Toast.LENGTH_SHORT).show()
    }

    private fun dismissDynamicIsland() {
        DynamicIslandService.hideIsland(this)
        Toast.makeText(this, "Dynamic Island dismissed", Toast.LENGTH_SHORT).show()
    }

    private fun simulateReelSwipe() {
        val nextCount = repository.todayCount.value + 1
        lifecycleScope.launch {
            repository.recordReel(Platform.INSTAGRAM)
        }
        if (isOverlayGrantedState.value) {
            DynamicIslandService.showIsland(this, Platform.INSTAGRAM)
            DynamicIslandService.updateLiveCount(this, Platform.INSTAGRAM, nextCount)
        }
        Toast.makeText(this, "📱 +1 Reel swiped! Intake dial updated.", Toast.LENGTH_SHORT).show()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }
}
