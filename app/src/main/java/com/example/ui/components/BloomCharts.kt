package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSecondary
import com.example.ui.theme.BloomSoftLavender

@Composable
fun BloomLineChartCard(
    title: String,
    dataPoints: List<Pair<String, Float>>, // Label/Date -> Percentage (0..100)
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete habits to see daily trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val lineColor = BloomPrimary
                val gradientStart = BloomSecondary.copy(alpha = 0.35f)
                val gradientEnd = Color.Transparent
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = h - bottomPadding

                    // Draw horizontal grid lines (0%, 50%, 100%)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, 0f),
                        end = Offset(w, 0f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, chartHeight / 2),
                        end = Offset(w, chartHeight / 2),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(w, chartHeight),
                        strokeWidth = 1f
                    )

                    val stepX = if (dataPoints.size > 1) w / (dataPoints.size - 1) else w

                    val points = dataPoints.mapIndexed { index, pair ->
                        val x = index * stepX
                        val y = chartHeight - (pair.second.coerceIn(0f, 100f) / 100f * chartHeight)
                        Offset(x, y)
                    }

                    // Build smooth cubic bezier path
                    val linePath = Path()
                    val fillPath = Path()

                    if (points.isNotEmpty()) {
                        linePath.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, chartHeight)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val control1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val control2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                            linePath.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                            fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                        }

                        fillPath.lineTo(points.last().x, chartHeight)
                        fillPath.close()

                        // Draw gradient fill under line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientStart, gradientEnd),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )

                        // Draw line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw small dots on points
                        for (p in points) {
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = p)
                            drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = p)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloomBarChartCard(
    title: String,
    bars: List<Pair<String, Float>>, // Label -> Percentage (0..100)
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (bars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Weekly performance will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val primaryColor = BloomPrimary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val barWidth = 28.dp.toPx()
                    val totalBars = bars.size
                    val spacing = (w - (barWidth * totalBars)) / (totalBars + 1).coerceAtLeast(1)

                    bars.forEachIndexed { i, pair ->
                        val x = spacing + i * (barWidth + spacing)
                        val barHeight = (pair.second.coerceIn(0f, 100f) / 100f) * (h - 20f)
                        val y = h - barHeight

                        // Background pillar
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(14f, 14f)
                        )

                        // Filled bar with rounded top
                        if (barHeight > 4f) {
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(14f, 14f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Labels row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    bars.forEach { (label, pct) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${pct.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BloomPrimary
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
