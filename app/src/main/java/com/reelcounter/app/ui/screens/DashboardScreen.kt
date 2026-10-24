package com.reelcounter.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelcounter.app.data.model.DailyStats
import com.reelcounter.app.data.model.DangerLevel
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.repository.DisplayMode
import com.reelcounter.app.ui.theme.*

@Composable
fun DashboardScreen(
    stats: DailyStats?,
    isAccessibilityEnabled: Boolean,
    isOverlayGranted: Boolean,
    enabledPlatforms: Set<Platform>,
    currentDisplayMode: DisplayMode,
    hasCameraCutout: Boolean,
    onTogglePlatform: (Platform, Boolean) -> Unit,
    onChangeDisplayMode: (DisplayMode) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onTestDynamicIsland: () -> Unit,
    onDismissDynamicIsland: () -> Unit,
    onSimulateReelSwipe: () -> Unit
) {
    val totalCount = stats?.totalCount ?: 0
    val targetLimit = 50
    val currentDangerLevel = when {
        totalCount < 20 -> DangerLevel.MINDFUL
        totalCount < 50 -> DangerLevel.MODERATE
        totalCount < 80 -> DangerLevel.WARNING
        else -> DangerLevel.DOOMSCROLL
    }

    val progressFraction = (totalCount.toFloat() / targetLimit.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 900),
        label = "progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FEEDBRAKE",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Reel Intake Monitor",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(currentDangerLevel.hexColor).copy(alpha = 0.15f))
                        .border(1.dp, Color(currentDangerLevel.hexColor).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentDangerLevel.label,
                        color = Color(currentDangerLevel.hexColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Circular Dial Progress
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkSurface, DarkCard)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(190.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = 1f,
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.06f),
                            strokeWidth = 12.dp,
                            trackColor = Color.Transparent
                        )
                        CircularProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxSize(),
                            color = Color(currentDangerLevel.hexColor),
                            strokeWidth = 12.dp,
                            trackColor = Color.Transparent
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalCount.toString(),
                                color = TextPrimary,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "/ $targetLimit reels goal",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val minutesStr = "${stats?.totalWatchTimeMinutes ?: (totalCount * 25 / 60)} min"
                        val zenStr = "${stats?.zenScore ?: 100}%"
                        QuickStatItem(label = "Estimated Time", value = minutesStr)
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        QuickStatItem(label = "Mindful Score", value = zenStr)
                    }
                }
            }
        }

        // ==========================================
        // FEATURE 1: APP SELECTOR (Choose what to track)
        // ==========================================
        item {
            Text(
                text = "🎯 CHOOSE APPS TO TRACK",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select only the apps you want Feedbrake to monitor. Other apps and phone menus are strictly ignored.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                listOf(
                    Platform.INSTAGRAM to "Instagram Reels",
                    Platform.TIKTOK to "TikTok Short Videos",
                    Platform.YOUTUBE_SHORTS to "YouTube Shorts",
                    Platform.FACEBOOK to "Facebook Reels",
                    Platform.SNAPCHAT to "Snapchat Spotlight",
                    Platform.REDDIT to "Reddit Video Feeds"
                ).forEach { (platform, desc) ->
                    val isChecked = enabledPlatforms.contains(platform)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = platform.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = platform.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = desc, color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = isChecked,
                            onCheckedChange = { onTogglePlatform(platform, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF1E2433)
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // FEATURE 2: ISLAND vs NOTIFICATION MODE
        // ==========================================
        item {
            Text(
                text = "📱 DISPLAY MODE & CUTOUT ADAPTATION",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (hasCameraCutout) "Camera punch-hole detected on your device." else "No punch-hole cutout detected. Live Notification panel is recommended.",
                    color = if (hasCameraCutout) EmeraldGreen else AmberWarning,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                DisplayMode.entries.forEach { mode ->
                    val isSelected = currentDisplayMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1B2433) else Color.Transparent)
                            .border(1.dp, if (isSelected) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .clickable { onChangeDisplayMode(mode) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onChangeDisplayMode(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = mode.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = mode.description, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Dynamic Island Testing Controls
        item {
            Text(
                text = "⚡ DYNAMIC ISLAND CONTROLS & TEST",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Live Punch-Hole Overlay",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Preview how the Dynamic Island wraps around your front camera punch-hole with live reel count and animations.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTestDynamicIsland,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Preview Island", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDismissDynamicIsland,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Dismiss", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onSimulateReelSwipe,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433), contentColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "📱 Simulate +1 Reel Swipe", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // ==========================================
        // FEATURE 3: WIDGET SHOWCASE & SIZES
        // ==========================================
        item {
            Text(
                text = "🧩 HOME SCREEN WIDGETS (3 STYLES)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WidgetStyleCard(
                    size = "2x1",
                    title = "Feedbrake Mini Pill",
                    description = "Minimalist quick pill widget showing current reel count and color status dot."
                )
                WidgetStyleCard(
                    size = "2x2",
                    title = "Feedbrake Focus Card",
                    description = "Standard square card with today's count, estimated time, and mindful tag."
                )
                WidgetStyleCard(
                    size = "4x2",
                    title = "Feedbrake Full Dashboard",
                    description = "Expanded banner with daily limit percentage, watch time, and per-platform stats."
                )
                Text(
                    text = "Tip: Long-press your phone's home screen -> Tap 'Widgets' -> Search 'Feedbrake' to place any widget.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Permissions
        item {
            Text(
                text = "SYSTEM PERMISSIONS STATUS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PermissionActionRow(
                    title = "1. Accessibility Service",
                    subtitle = if (isAccessibilityEnabled) "Active & Tracking selected reels only" else "Required to detect reel scrolls in selected apps",
                    isGranted = isAccessibilityEnabled,
                    buttonText = if (isAccessibilityEnabled) "Active" else "Enable",
                    onClick = onOpenAccessibilitySettings
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                PermissionActionRow(
                    title = "2. Dynamic Island Overlay",
                    subtitle = if (isOverlayGranted) "Active & Allowed to draw around punch-hole" else "Required if using Dynamic Island pill overlay",
                    isGranted = isOverlayGranted,
                    buttonText = if (isOverlayGranted) "Granted" else "Grant",
                    onClick = onOpenOverlaySettings
                )
            }
        }
    }
}

@Composable
fun WidgetStyleCard(size: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141A25))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NeonCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = size, color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun PermissionActionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isGranted) "OK " else "! ",
                    fontSize = 12.sp,
                    color = if (isGranted) Color(0xFF00E676) else Color(0xFFFFB300),
                    fontWeight = FontWeight.Bold
                )
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0xFF1E2822) else NeonCyan,
                contentColor = if (isGranted) Color(0xFF00E676) else DarkBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
