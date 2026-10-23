package com.reelcounter.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
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
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit
) {
    val totalCount = stats?.totalCount ?: 0
    val targetLimit = 50
    val progress = (totalCount.toFloat() / targetLimit).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_dial"
    )

    val currentDangerLevel = stats?.dangerLevel ?: DangerLevel.MINDFUL

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // === HEADER ===
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Today's Intake",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Zen score pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(currentDangerLevel.hexColor).copy(alpha = 0.15f))
                        .border(1.dp, Color(currentDangerLevel.hexColor).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentDangerLevel.label,
                        color = Color(currentDangerLevel.hexColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // === HERO DIAL CARD ===
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
                    // Circular Progress Dial
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
                                text = "\$totalCount",
                                color = TextPrimary,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "/ \$targetLimit reels goal",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickStatItem(
                            label = "Estimated Time",
                            value = "\${stats?.totalWatchTimeMinutes ?: 0} min"
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        QuickStatItem(
                            label = "Mindful Score",
                            value = "\${stats?.zenScore ?: 100}%"
                        )
                    }
                }
            }
        }

        // === PLATFORM BREAKDOWN SECTION ===
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

        // === PERMISSIONS / SETUP CARD ===
        item {
            Text(
                text = "DYNAMIC ISLAND SETUP",
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
                    subtitle = "Detects scrolls in Instagram, TikTok, Shorts & Facebook",
                    buttonText = "Enable",
                    onClick = onOpenAccessibilitySettings
                )
                Divider(color = Color.White.copy(alpha = 0.08f))
                PermissionActionRow(
                    title = "2. Dynamic Island Overlay",
                    subtitle = "Draws the pill around your camera punch-hole",
                    buttonText = "Grant",
                    onClick = onOpenOverlaySettings
                )
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
                Text(text = "\$percentage% of total", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Text(
            text = "\$count",
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
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
