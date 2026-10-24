package com.reelcounter.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.reelcounter.app.ui.theme.*

@Composable
fun DashboardScreen(
    stats: DailyStats?,
    isAccessibilityEnabled: Boolean,
    isOverlayGranted: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onTestDynamicIsland: () -> Unit,
    onDismissDynamicIsland: () -> Unit,
    onSimulateReelSwipe: () -> Unit
) {
    val totalCount = stats?.totalCount ?: 0
    val targetLimit = 50
    val currentDangerLevel = DangerLevel.fromCount(totalCount)

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
                        text = currentDangerLevel.displayName,
                        color = Color(currentDangerLevel.hexColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkSurface, DarkSurfaceVariant)
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
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.06f),
                            strokeWidth = 12.dp,
                            trackColor = Color.Transparent
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
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
                        val minutesStr = "${stats?.totalWatchTimeMinutes ?: 0} min"
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
                    subtitle = if (isAccessibilityEnabled) "Active & Tracking reels in background" else "Required to detect reels in Instagram, TikTok, Shorts & Facebook",
                    isGranted = isAccessibilityEnabled,
                    buttonText = if (isAccessibilityEnabled) "Active" else "Enable",
                    onClick = onOpenAccessibilitySettings
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                PermissionActionRow(
                    title = "2. Dynamic Island Overlay",
                    subtitle = if (isOverlayGranted) "Active & Allowed to draw around punch-hole" else "Required to draw the live pill over other apps",
                    isGranted = isOverlayGranted,
                    buttonText = if (isOverlayGranted) "Granted" else "Grant",
                    onClick = onOpenOverlaySettings
                )
            }
        }

        item {
            Text(
                text = "PLATFORM BREAKDOWN",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        item {
            val breakdown = stats?.platformCounts ?: emptyMap()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Platform.INSTAGRAM,
                    Platform.TIKTOK,
                    Platform.YOUTUBE_SHORTS,
                    Platform.FACEBOOK
                ).forEach { platform ->
                    val platformCount = breakdown[platform] ?: 0
                    PlatformRowCard(platform = platform, count = platformCount, total = totalCount)
                }
            }
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
fun PlatformRowCard(platform: Platform, count: Int, total: Int) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(platform.brandHexColor).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = platform.iconEmoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = platform.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "$percentage% of total", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Text(
            text = count.toString(),
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
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
