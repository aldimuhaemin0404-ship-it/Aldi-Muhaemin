package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.IncomeEntity
import com.example.domain.KasUiState
import com.example.ui.components.DailyIncomeBarChartCard
import com.example.ui.components.IncomeRowCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatRupiah

@Composable
fun BerandaScreen(
    state: KasUiState,
    onAddIncomeClick: () -> Unit,
    onEditIncomeClick: (IncomeEntity) -> Unit,
    onEditDailyTargetClick: () -> Unit,
    onEditSavingsGoalClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onShiftBarWeek: (Int) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPockets: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("beranda_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KasHarian",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kelola pemasukan & simpanan harian",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onManageCategoriesClick,
                    modifier = Modifier.testTag("home_manage_categories_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kategori")
                }
            }
        }

        // Hero Balance & Daily Target Card with atmospheric banner background
        item {
            val targetProgress = if (state.dailyTarget > 0L) {
                (state.todayTotal.toFloat() / state.dailyTarget.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val targetPercent = if (state.dailyTarget > 0L) {
                ((state.todayTotal.toFloat() / state.dailyTarget.toFloat()) * 100).toInt()
            } else 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vault_banner),
                    contentDescription = "Latar kartu saldo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xD9073B2B),
                                    Color(0xEB0A4F3A)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Pemasukan Tersimpan",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFD1FAE5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupiah(state.totalBalance),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        if (state.activeDaysStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FDE68A)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak harian",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${state.activeDaysStreak} hr",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFFEF3C7)
                                    )
                                }
                            }
                        }
                    }

                    // Today vs Target section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x24FFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onEditDailyTargetClick)
                            .testTag("edit_daily_target_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Pemasukan Hari Ini",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD1FAE5)
                                    )
                                    Text(
                                        text = formatRupiah(state.todayTotal),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Target: ${formatRupiah(state.dailyTarget)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFEF3C7)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Ubah target harian",
                                        tint = Color(0xFFFEF3C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { targetProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF6CE0B3),
                                trackColor = Color(0x33FFFFFF),
                                strokeCap = StrokeCap.Round
                            )

                            Text(
                                text = if (targetPercent >= 100) {
                                    "Target harian tercapai ($targetPercent%)"
                                } else {
                                    "$targetPercent% dari target harian tercapai"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                    }

                    Button(
                        onClick = onAddIncomeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6CE0B3),
                            contentColor = Color(0xFF003827)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("hero_add_income_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catat Pemasukan Hari Ini",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Savings Goal Progress Card (based on total income)
        item {
            com.example.ui.components.SavingsGoalCard(
                goal = state.savingsGoal,
                onEditGoalClick = onEditSavingsGoalClick
            )
        }

        // Quick Period Summary Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard(
                    title = "7 Hari Terakhir",
                    amount = formatRupiah(state.weekTotal),
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Bulan Ini",
                    amount = formatRupiah(state.monthTotal),
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Storage Pockets Horizontal Preview
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kantong Penyimpanan",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onNavigateToPockets) {
                        Text("Lihat Semua")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.pockets, key = { it.pocket.id }) { item ->
                        val pColor = KasColorCatalog.toColor(item.pocket.colorHex)
                        Card(
                            modifier = Modifier
                                .width(172.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(onClick = onNavigateToPockets),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(pColor.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = KasIconCatalog.resolveIcon(item.pocket.iconKey),
                                        contentDescription = item.pocket.name,
                                        tint = pColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = item.pocket.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatRupiah(item.balance),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Bar Chart directly on Home
        item {
            DailyIncomeBarChartCard(
                points = state.dailyBarPoints,
                rangeLabel = state.barChartRangeLabel,
                weekOffset = state.weekOffset,
                dailyTarget = state.dailyTarget,
                onShiftWeek = onShiftBarWeek
            )
        }

        // Today's Income Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pemasukan Hari Ini (${state.todayIncomes.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onNavigateToAnalytics) {
                    Text("Tren Lengkap")
                }
            }
        }

        if (state.todayIncomes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_income),
                            contentDescription = "Belum ada pemasukan hari ini",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp, 110.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Text(
                            text = "Belum ada pemasukan hari ini",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Catat pemasukan harian pertama Anda dan simpan ke kantong pilihan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.todayIncomes, key = { it.income.id }) { item ->
                IncomeRowCard(
                    item = item,
                    onClick = { onEditIncomeClick(item.income) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    amount: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
