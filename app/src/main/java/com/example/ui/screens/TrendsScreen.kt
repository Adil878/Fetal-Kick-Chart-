package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KickSessionEntity
import com.example.ui.DailyTrendPoint
import com.example.ui.TimeOfDayDistribution

@Composable
fun TrendsScreen(
    sessions: List<KickSessionEntity>,
    trendPoints: List<DailyTrendPoint>,
    distribution: TimeOfDayDistribution,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val totalSessions = sessions.size
            val avgDurationMins = if (sessions.isNotEmpty()) {
                (sessions.map { it.durationSeconds }.average() / 60).toInt()
            } else 0
            val completedTargetCount = sessions.count { it.isTargetReached }
            val completionRate = if (totalSessions > 0) (completedTargetCount * 100 / totalSessions) else 0

            StatCard(
                title = "Total Sessions",
                value = "$totalSessions",
                subtext = "Tracked logs",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Duration",
                value = "${avgDurationMins}m",
                subtext = "Per session",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Completion",
                value = "$completionRate%",
                subtext = "Met goal",
                modifier = Modifier.weight(1f)
            )
        }

        val totalWaterGlasses = sessions.sumOf { it.waterIntakeGlasses }
        if (totalWaterGlasses > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💧", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Maternal Hydration Tracked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$totalWaterGlasses glasses (${totalWaterGlasses * 250} ml) consumed during sessions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 1. Session Duration Trend (Line Chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Duration Trend Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Minutes required to reach kick target in recent sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                if (trendPoints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Record sessions to see trend graph",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    DurationLineChart(
                        trendPoints = trendPoints,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }

        // 2. Circadian Movement Distribution (Bar Chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Baby's Activity by Time of Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Identifies peak circadian movement windows",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                CircadianDistributionBarChart(
                    distribution = distribution,
                    barColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DurationLineChart(
    trendPoints: List<DailyTrendPoint>,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (trendPoints.maxOfOrNull { it.durationMinutes } ?: 60f).coerceAtLeast(30f)

    Canvas(modifier = modifier.padding(bottom = 24.dp, top = 8.dp, start = 8.dp, end = 8.dp)) {
        val width = size.width
        val height = size.height

        // Horizontal grid lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        if (trendPoints.size == 1) {
            val pt = trendPoints[0]
            val cx = width / 2f
            val cy = height - (pt.durationMinutes / maxMinutes) * height
            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(cx, cy))
            return@Canvas
        }

        val stepX = width / (trendPoints.size - 1)
        val path = Path()
        val fillPath = Path()

        val points = trendPoints.mapIndexed { index, point ->
            val x = index * stepX
            val y = (height - (point.durationMinutes / maxMinutes) * height).coerceIn(0f, height)
            Offset(x, y)
        }

        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, height)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val cx = (p0.x + p1.x) / 2
            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(points.last().x, height)
        fillPath.close()

        // Soft gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Bold stroke line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points with indicator
        points.forEachIndexed { idx, offset ->
            val isTargetReached = trendPoints[idx].targetReached
            drawCircle(
                color = if (isTargetReached) primaryColor else Color(0xFFE5A93B),
                radius = 4.5.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = offset
            )
        }
    }
}

@Composable
fun CircadianDistributionBarChart(
    distribution: TimeOfDayDistribution,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Morning\n6am-12pm" to distribution.morningKicks,
        "Afternoon\n12pm-6pm" to distribution.afternoonKicks,
        "Evening\n6pm-12am" to distribution.eveningKicks,
        "Night\n12am-6am" to distribution.nightKicks
    )

    val maxVal = categories.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(10f) ?: 10f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barCount = categories.size
            val slotWidth = totalWidth / barCount
            val barWidth = slotWidth * 0.45f

            categories.forEachIndexed { index, (label, count) ->
                val barHeight = (count.toFloat() / maxVal) * (totalHeight - 20f)
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = totalHeight - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categories.forEach { (label, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$count kicks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = label.split("\n")[0],
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
