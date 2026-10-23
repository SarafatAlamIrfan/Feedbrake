package com.reelcounter.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.ui.screens.DashboardScreen
import com.reelcounter.app.ui.theme.ReelCounterTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: ReelRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ReelRepository.getInstance(this)

        setContent {
            ReelCounterTheme {
                val stats by repository.todayStats.collectAsState(initial = null)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        stats = stats,
                        onOpenAccessibilitySettings = { openAccessibilitySettings() },
                        onOpenOverlaySettings = { openOverlaySettings() }
                    )
                }
            }
        }
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
}
