package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.KasUiState
import com.example.domain.TrendPeriod
import com.example.ui.components.CategoryDistributionCard
import com.example.ui.components.DailyIncomeBarChartCard
import com.example.ui.components.IncomeTrendLineChartCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.formatRupiah

@Composable
fun AnalitikScreen(
    state: KasUiState,
    onShiftBarWeek: (Int) -> Unit,
    onSelectTrendPeriod: (TrendPeriod) -> Unit,
    onManageCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highestDay = state.dailyBarPoints.maxByOrNull { it.totalAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analitik_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visualisasi & Analitik",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Pantau grafik batang harian & tren garis berkala",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onManageCategoriesClick,
                    modifier = Modifier.testTag("analytics_manage_categories_button")
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kategori")
                }
            }
        }

        // Key Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "Rata-rata / Hari",
                    value = formatRupiah(state.dailyAverageThisMonth),
                    subtext = "Bulan berjalan",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Puncak Harian",
                    value = formatRupiah(highestDay?.totalAmount ?: 0L),
                    subtext = if (highestDay != null && highestDay.totalAmount > 0L) {
                        "${highestDay.dayShort}, ${highestDay.dateLabel}"
                    } else {
                        "Belum ada data"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily Income Bar Chart
        item {
            DailyIncomeBarChartCard(
                points = state.dailyBarPoints,
                rangeLabel = state.barChartRangeLabel,
                weekOffset = state.weekOffset,
                dailyTarget = state.dailyTarget,
                onShiftWeek = onShiftBarWeek
            )
        }

        // Weekly / Monthly Line Chart
        item {
            IncomeTrendLineChartCard(
                points = state.trendLinePoints,
                trendPeriod = state.trendPeriod,
                onSelectPeriod = onSelectTrendPeriod
            )
        }

        // Category Distribution Breakdown
        item {
            CategoryDistributionCard(
                summaries = state.categorySummaries,
                onManageCategories = onManageCategoriesClick
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
