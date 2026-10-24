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
import com.reelcounter.app.data.repository.ReelRepository
import kotlinx.coroutines.flow.first

class ReelGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ReelRepository.getInstance(context)
        val todayCount = repo.todayCount.first()

        provideContent {
            WidgetContent(count = todayCount)
        }
    }

    @Composable
    private fun WidgetContent(count: Int) {
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
                text = "FEEDBRAKE - REELS",
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
                count < 50 -> "Moderate"
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
