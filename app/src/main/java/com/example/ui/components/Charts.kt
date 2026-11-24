package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySummary
import com.example.domain.DailyBarPoint
import com.example.domain.TrendLinePoint
import com.example.domain.TrendPeriod
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah
import kotlin.math.abs

@Composable
fun DailyIncomeBarChartCard(
    points: List<DailyBarPoint>,
    rangeLabel: String,
    weekOffset: Int,
    dailyTarget: Long,
    onShiftWeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points) {
        val todayIdx = points.indexOfLast { it.isToday }
        val nonZeroIdx = points.indexOfLast { it.totalAmount > 0L }
        mutableIntStateOf(
            when {
                todayIdx >= 0 -> todayIdx
                nonZeroIdx >= 0 -> nonZeroIdx
                else -> (points.size - 1).coerceAtLeast(0)
            }
        )
    }

    val selectedPoint = points.getOrNull(selectedIndex)
    val maxData = points.maxOfOrNull { it.totalAmount } ?: 0L
    val chartMax = maxOf(maxData, dailyTarget, 100_000L).let { raw ->
        // Round up neatly for clean grid ticks
        val step = when {
            raw >= 5_000_000L -> 1_000_000L
            raw >= 1_000_000L -> 250_000L
            raw >= 250_000L -> 50_000L
            else -> 25_000L
        }
        ((raw + step - 1) / step) * step
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryBarColor = primaryColor.copy(alpha = 0.38f)
    val zeroBarColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val targetLineColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeLabelColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_bar_chart_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grafik Pemasukan Harian",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onShiftWeek(-1) },
                        modifier = Modifier.testTag("bar_chart_prev_week")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Minggu sebelumnya"
                        )
                    }
                    IconButton(
                        onClick = { onShiftWeek(1) },
                        enabled = weekOffset < 0,
                        modifier = Modifier.testTag("bar_chart_next_week")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Minggu berikutnya"
                        )
                    }
                }
            }

            if (selectedPoint != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (selectedPoint.isToday) {
                                    "Hari Ini (${selectedPoint.dateLabel})"
                                } else {
                                    "${selectedPoint.dayShort}, ${selectedPoint.dateLabel}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatRupiah(selectedPoint.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (dailyTarget > 0L) {
                            val pct = ((selectedPoint.totalAmount.toFloat() / dailyTarget.toFloat()) * 100).toInt()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pct >= 100) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Text(
                                    text = "$pct% target",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pct >= 100) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550),
                label = "barAnim"
            )

            val axisTextStyle = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = labelColor
            )
            val dayTextStyle = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.isEmpty()) return@detectTapGestures
                                val leftPad = 56.dp.toPx()
                                val chartWidth = (size.width - leftPad).coerceAtLeast(1f)
                                val slotWidth = chartWidth / points.size
                                val tapped = ((offset.x - leftPad) / slotWidth).toInt()
                                if (tapped in points.indices) {
                                    selectedIndex = tapped
                                }
                            }
                        }
                ) {
                    val leftPad = 56.dp.toPx()
                    val topPad = 12.dp.toPx()
                    val bottomPad = 30.dp.toPx()
                    val chartW = (size.width - leftPad - 8.dp.toPx()).coerceAtLeast(1f)
                    val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                    // Horizontal grid lines & Y-axis labels (0%, 50%, 100%)
                    val steps = 3
                    for (i in 0..steps) {
                        val ratio = i.toFloat() / steps.toFloat()
                        val y = topPad + chartH * (1f - ratio)
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPad, y),
                            end = Offset(leftPad + chartW, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = if (i > 0) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
                        )
                        val tickVal = (chartMax * ratio).toLong()
                        val tickText = formatCompactRupiah(tickVal)
                        val measured = textMeasurer.measure(tickText, axisTextStyle)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                x = (leftPad - measured.size.width - 8.dp.toPx()).coerceAtLeast(0f),
                                y = y - measured.size.height / 2f
                            )
                        )
                    }

                    // Daily Target reference dashed line
                    if (dailyTarget in 1..chartMax) {
                        val targetY = topPad + chartH * (1f - dailyTarget.toFloat() / chartMax.toFloat())
                        drawLine(
                            color = targetLineColor,
                            start = Offset(leftPad, targetY),
                            end = Offset(leftPad + chartW, targetY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    }

                    // Bars
                    if (points.isNotEmpty()) {
                        val slotWidth = chartW / points.size
                        val barWidth = (slotWidth * 0.54f).coerceIn(14.dp.toPx(), 34.dp.toPx())

                        points.forEachIndexed { index, point ->
                            val centerX = leftPad + slotWidth * index + slotWidth / 2f
                            val isSelected = index == selectedIndex
                            val ratio = (point.totalAmount.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                            val barH = if (point.totalAmount == 0L) {
                                4.dp.toPx()
                            } else {
                                (chartH * ratio * animatedProgress).coerceAtLeast(8.dp.toPx())
                            }
                            val topY = topPad + chartH - barH

                            val brush = when {
                                point.totalAmount == 0L -> Brush.verticalGradient(listOf(zeroBarColor, zeroBarColor))
                                isSelected -> Brush.verticalGradient(
                                    listOf(primaryColor, Color(0xFF16A36E))
                                )
                                else -> Brush.verticalGradient(
                                    listOf(secondaryBarColor, secondaryBarColor.copy(alpha = 0.25f))
                                )
                            }

                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(centerX - barWidth / 2f, topY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Day label below bar
                            val dayMeasured = textMeasurer.measure(
                                text = point.dayShort,
                                style = dayTextStyle.copy(
                                    color = if (isSelected) activeLabelColor else labelColor,
                                    fontWeight = if (isSelected || point.isToday) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                            drawText(
                                textLayoutResult = dayMeasured,
                                topLeft = Offset(
                                    x = centerX - dayMeasured.size.width / 2f,
                                    y = topPad + chartH + 8.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }

            if (dailyTarget > 0L) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(targetLineColor, RoundedCornerShape(1.dp))
                    )
                    Text(
                        text = "Garis target harian: ${formatRupiah(dailyTarget)} • Ketuk batang untuk detail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeTrendLineChartCard(
    points: List<TrendLinePoint>,
    trendPeriod: TrendPeriod,
    onSelectPeriod: (TrendPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points) {
        val lastNonZero = points.indexOfLast { it.totalAmount > 0L }
        mutableIntStateOf(if (lastNonZero >= 0) lastNonZero else (points.size - 1).coerceAtLeast(0))
    }
    val selectedPoint = points.getOrNull(selectedIndex)
    val maxData = points.maxOfOrNull { it.totalAmount } ?: 0L
    val chartMax = maxOf(maxData, 200_000L).let { raw ->
        val step = when {
            raw >= 10_000_000L -> 2_500_000L
            raw >= 2_000_000L -> 500_000L
            else -> 100_000L
        }
        ((raw + step - 1) / step) * step
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val accentNodeColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textMeasurer = rememberTextMeasurer()

    val animFraction by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600),
        label = "lineAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_line_chart_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ringkasan Tren Pemasukan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (trendPeriod == TrendPeriod.WEEKLY) {
                            "Tren 8 minggu terakhir"
                        } else {
                            "Tren 6 bulan terakhir"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TrendPeriod.entries.forEachIndexed { idx, period ->
                    SegmentedButton(
                        selected = trendPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        shape = SegmentedButtonDefaults.itemShape(idx, TrendPeriod.entries.size),
                        modifier = Modifier.testTag("trend_toggle_${period.name.lowercase()}")
                    ) {
                        Text(period.label)
                    }
                }
            }

            if (selectedPoint != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedPoint.detailLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = formatRupiah(selectedPoint.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${selectedPoint.entryCount} catatan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val axisTextStyle = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = labelColor
            )
            val xLabelStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.isEmpty()) return@detectTapGestures
                                val leftPad = 56.dp.toPx()
                                val rightPad = 14.dp.toPx()
                                val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                                val count = points.size
                                val stepX = if (count > 1) chartW / (count - 1) else chartW
                                var bestIdx = 0
                                var bestDist = Float.MAX_VALUE
                                for (i in points.indices) {
                                    val px = leftPad + stepX * i
                                    val dist = abs(offset.x - px)
                                    if (dist < bestDist) {
                                        bestDist = dist
                                        bestIdx = i
                                    }
                                }
                                selectedIndex = bestIdx
                            }
                        }
                ) {
                    val leftPad = 56.dp.toPx()
                    val rightPad = 14.dp.toPx()
                    val topPad = 14.dp.toPx()
                    val bottomPad = 30.dp.toPx()
                    val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                    // Grid lines & Y-axis labels
                    val steps = 3
                    for (i in 0..steps) {
                        val ratio = i.toFloat() / steps.toFloat()
                        val y = topPad + chartH * (1f - ratio)
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPad, y),
                            end = Offset(leftPad + chartW, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = if (i > 0) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
                        )
                        val tickText = formatCompactRupiah((chartMax * ratio).toLong())
                        val measured = textMeasurer.measure(tickText, axisTextStyle)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                x = (leftPad - measured.size.width - 8.dp.toPx()).coerceAtLeast(0f),
                                y = y - measured.size.height / 2f
                            )
                        )
                    }

                    if (points.isNotEmpty()) {
                        val stepX = if (points.size > 1) chartW / (points.size - 1) else 0f
                        val coords = points.mapIndexed { index, pt ->
                            val x = if (points.size == 1) leftPad + chartW / 2f else leftPad + stepX * index
                            val ratio = (pt.totalAmount.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                            val y = topPad + chartH * (1f - ratio * animFraction)
                            Offset(x, y)
                        }

                        // Smooth Bezier Path + Gradient Fill
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(coords.last().x, topPad + chartH)
                            lineTo(coords.first().x, topPad + chartH)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.28f),
                                    lineColor.copy(alpha = 0.02f)
                                ),
                                startY = topPad,
                                endY = topPad + chartH
                            )
                        )

                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Selected vertical guide
                        val selCoord = coords.getOrNull(selectedIndex)
                        if (selCoord != null) {
                            drawLine(
                                color = lineColor.copy(alpha = 0.35f),
                                start = Offset(selCoord.x, topPad),
                                end = Offset(selCoord.x, topPad + chartH),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // Nodes & X-axis labels
                        coords.forEachIndexed { index, pt ->
                            val isSelected = index == selectedIndex
                            if (isSelected) {
                                drawCircle(
                                    color = accentNodeColor.copy(alpha = 0.22f),
                                    radius = 10.dp.toPx(),
                                    center = pt
                                )
                            }
                            drawCircle(
                                color = if (isSelected) accentNodeColor else lineColor,
                                radius = if (isSelected) 5.5.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = surfaceColor,
                                radius = if (isSelected) 2.5.dp.toPx() else 1.8.dp.toPx(),
                                center = pt
                            )

                            val xText = points[index].label
                            val measuredX = textMeasurer.measure(
                                text = xText,
                                style = xLabelStyle.copy(
                                    color = if (isSelected) lineColor else labelColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                            drawText(
                                textLayoutResult = measuredX,
                                topLeft = Offset(
                                    x = (pt.x - measuredX.size.width / 2f)
                                        .coerceIn(leftPad - 8.dp.toPx(), size.width - measuredX.size.width),
                                    y = topPad + chartH + 8.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ketuk titik pada grafik untuk melihat rincian periode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryDistributionCard(
    summaries: List<CategorySummary>,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSummaries = summaries.filter { it.totalAmount > 0L }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_distribution_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Distribusi per Kategori",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Komposisi sumber pemasukan Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onManageCategories,
                    modifier = Modifier.testTag("manage_categories_inline_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kelola")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeSummaries.isEmpty()) {
                Text(
                    text = "Belum ada pemasukan tercatat. Tambahkan pemasukan untuk melihat komposisi kategori.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Proportional segmented horizontal bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    activeSummaries.forEach { summary ->
                        Box(
                            modifier = Modifier
                                .weight(summary.percentage.coerceAtLeast(0.02f))
                                .fillMaxSize()
                                .background(KasColorCatalog.toColor(summary.category.colorHex))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activeSummaries.forEach { summary ->
                        val catColor = KasColorCatalog.toColor(summary.category.colorHex)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = KasIconCatalog.resolveIcon(summary.category.iconKey),
                                    contentDescription = summary.category.name,
                                    tint = catColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = summary.category.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${summary.count} transaksi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatRupiah(summary.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${(summary.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = catColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
