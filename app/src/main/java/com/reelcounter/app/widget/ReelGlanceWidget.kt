package com.reelcounter.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.reelcounter.app.data.model.Platform
import com.reelcounter.app.data.repository.ReelRepository
import kotlinx.coroutines.flow.first

// ==========================================
// 1. STANDARD FOCUS WIDGET (2x2)
// ==========================================
class ReelGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ReelRepository.getInstance(context)
        val todayCount = repo.todayCount.first()

        provideContent {
            StandardWidgetContent(count = todayCount)
        }
    }

    @Composable
    private fun StandardWidgetContent(count: Int) {
        val accentColor = if (count > 50) Color(0xFFFF3366) else Color(0xFF00F2FE)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF10131B))
                .padding(14.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "FEEDBRAKE • REELS",
                style = TextStyle(
                    color = ColorProvider(day = Color.White.copy(alpha = 0.5f), night = Color.White.copy(alpha = 0.5f)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = count.toString(),
                style = TextStyle(
                    color = ColorProvider(day = accentColor, night = accentColor),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            val statusText = when {
                count < 20 -> "Mindful Focus"
                count < 50 -> "Moderate Intake"
                else -> "Doomscroll Alert"
            }

            Text(
                text = statusText,
                style = TextStyle(
                    color = ColorProvider(day = Color.White.copy(alpha = 0.85f), night = Color.White.copy(alpha = 0.85f)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

class ReelWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReelGlanceWidget()
}

// ==========================================
// 2. COMPACT PILL WIDGET (2x1)
// ==========================================
class ReelCompactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ReelRepository.getInstance(context)
        val todayCount = repo.todayCount.first()

        provideContent {
            CompactWidgetContent(count = todayCount)
        }
    }

    @Composable
    private fun CompactWidgetContent(count: Int) {
        val meterColor = when {
            count < 20 -> Color(0xFF00E676)
            count < 50 -> Color(0xFFFFB300)
            else -> Color(0xFFFF3366)
        }

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF10131B))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "⚡",
                style = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = "$count",
                style = TextStyle(
                    color = ColorProvider(day = meterColor, night = meterColor),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.width(6.dp))

            Text(
                text = "reels",
                style = TextStyle(
                    color = ColorProvider(day = Color.White.copy(alpha = 0.7f), night = Color.White.copy(alpha = 0.7f)),
                    fontSize = 13.sp
                )
            )
        }
    }
}

class ReelCompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReelCompactWidget()
}

// ==========================================
// 3. DETAILED DASHBOARD WIDGET (4x2)
// ==========================================
class ReelDetailedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ReelRepository.getInstance(context)
        val count = repo.todayCount.first()
        val stats = repo.todayStats.first()
        val breakdown = repo.todayPlatformBreakdown.first()

        provideContent {
            DetailedWidgetContent(count = count, minutes = stats?.totalWatchTimeMinutes ?: (count * 25 / 60), breakdown = breakdown)
        }
    }

    @Composable
    private fun DetailedWidgetContent(count: Int, minutes: Int, breakdown: Map<Platform, Int>) {
        val meterColor = when {
            count < 20 -> Color(0xFF00E676)
            count < 50 -> Color(0xFFFFB300)
            else -> Color(0xFFFF3366)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0D1017))
                .padding(14.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "FEEDBRAKE DASHBOARD",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF00F2FE), night = Color(0xFF00F2FE)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "${(count.toFloat() / 50f * 100).toInt()}% of goal",
                    style = TextStyle(
                        color = ColorProvider(day = meterColor, night = meterColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$count",
                        style = TextStyle(
                            color = ColorProvider(day = Color.White, night = Color.White),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "reels watched today",
                        style = TextStyle(
                            color = ColorProvider(day = Color.White.copy(alpha = 0.6f), night = Color.White.copy(alpha = 0.6f)),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Column(horizontalAlignment = Alignment.Horizontal.End) {
                    Text(
                        text = "~$minutes min",
                        style = TextStyle(
                            color = ColorProvider(day = Color.White, night = Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "estimated time spent",
                        style = TextStyle(
                            color = ColorProvider(day = Color.White.copy(alpha = 0.6f), night = Color.White.copy(alpha = 0.6f)),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                val ig = breakdown[Platform.INSTAGRAM] ?: 0
                val tt = breakdown[Platform.TIKTOK] ?: 0
                val yt = breakdown[Platform.YOUTUBE_SHORTS] ?: 0
                val fb = breakdown[Platform.FACEBOOK] ?: 0

                Text(
                    text = "IG: $ig  •  TT: $tt  •  YT: $yt  •  FB: $fb",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White.copy(alpha = 0.5f), night = Color.White.copy(alpha = 0.5f)),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

class ReelDetailedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReelDetailedWidget()
}
