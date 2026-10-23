package com.reelcounter.app.overlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelcounter.app.data.model.Platform
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DynamicIslandOverlay(
    platformState: StateFlow<Platform>,
    countState: StateFlow<Int>,
    onDismiss: () -> Unit
) {
    val platform by platformState.collectAsState()
    val count by countState.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    // Dopamine Meter Color shift based on count
    val meterColor by animateColorAsState(
        targetValue = when {
            count < 15 -> Color(0xFF00F2FE) // Cool Cyan
            count < 30 -> Color(0xFF00E676) // Healthy Green
            count < 50 -> Color(0xFFFFB300) // Caution Amber
            else -> Color(0xFFFF3366)       // Danger Doomscroll Red
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "meter_color"
    )

    // Spring animation for expanding / collapsing the Dynamic Island
    val islandWidth by animateDpAsState(
        targetValue = if (isExpanded) 340.dp else 190.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "island_width"
    )

    val islandHeight by animateDpAsState(
        targetValue = if (isExpanded) 160.dp else 42.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "island_height"
    )

    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .shadow(elevation = 18.dp, shape = RoundedCornerShape(26.dp), spotColor = meterColor)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF000000)) // True Pitch Black matching cutout
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isExpanded = !isExpanded }
            .width(islandWidth)
            .height(islandHeight)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isExpanded) {
            // === COMPACT DYNAMIC ISLAND (Wrapped around camera cutout) ===
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Wing: Platform Emoji & Glowing Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = platform.iconEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(meterColor)
                    )
                }

                // Center Gap: Accommodates Camera Punch-Hole
                Spacer(modifier = Modifier.weight(1f))

                // Right Wing: Live Count Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                        },
                        label = "count_anim"
                    ) { targetCount ->
                        Text(
                            text = "\$targetCount",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = " reels",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // === EXPANDED DYNAMIC ISLAND CARD ===
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = platform.iconEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = platform.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Live Tracking",
                                color = meterColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Collapse",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Hero Counter in Expanded View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "\$count Reels Today",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        val estimatedMinutes = (count * 25) / 60
                        Text(
                            text = "≈ \$estimatedMinutes mins elapsed",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    // Dopamine Status Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(meterColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (count > 50) "🔥 High Dopamine" else "✨ Mindful",
                            color = meterColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Dopamine Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (count / 60f).coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00F2FE), meterColor)
                                )
                            )
                    )
                }
            }
        }
    }
}
