package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.IncomeEntity
import com.example.domain.HistoryFilterPeriod
import com.example.domain.KasUiState
import com.example.ui.components.IncomeRowCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatDateGroupLabel
import com.example.util.formatRupiah
import com.example.util.startOfDayMillis

@Composable
fun RiwayatScreen(
    state: KasUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectPeriod: (HistoryFilterPeriod) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectPocket: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    onEditIncomeClick: (IncomeEntity) -> Unit,
    onManageCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grouped = remember(state.filteredHistoryIncomes) {
        state.filteredHistoryIncomes.groupBy { startOfDayMillis(it.income.dateMillis) }
            .toSortedMap(compareByDescending { it })
    }
    val filteredSum = remember(state.filteredHistoryIncomes) {
        state.filteredHistoryIncomes.sumOf { it.income.amount }
    }
    val hasActiveFilters = state.searchQuery.isNotBlank() ||
        state.selectedFilterPeriod != HistoryFilterPeriod.ALL ||
        state.selectedFilterCategoryId != null ||
        state.selectedFilterPocketId != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("riwayat_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Riwayat Pemasukan",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Cari & filter berdasarkan periode, kategori, atau kantong",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onManageCategoriesClick,
                    modifier = Modifier.testTag("history_manage_categories_button")
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kategori")
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Cari judul, catatan, kategori, kantong...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Cari") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input")
            )
        }

        // Period Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilterPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.selectedFilterPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        label = { Text(period.label) },
                        modifier = Modifier.testTag("filter_period_${period.name.lowercase()}")
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedFilterCategoryId == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("Semua Kategori") }
                )
                state.categories.forEach { category ->
                    val catColor = KasColorCatalog.toColor(category.colorHex)
                    FilterChip(
                        selected = state.selectedFilterCategoryId == category.id,
                        onClick = {
                            onSelectCategory(
                                if (state.selectedFilterCategoryId == category.id) null else category.id
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = KasIconCatalog.resolveIcon(category.iconKey),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(category.name) },
                        modifier = Modifier.testTag("filter_category_${category.id}")
                    )
                }
            }
        }

        // Pocket Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedFilterPocketId == null,
                    onClick = { onSelectPocket(null) },
                    label = { Text("Semua Kantong") }
                )
                state.pockets.forEach { p ->
                    FilterChip(
                        selected = state.selectedFilterPocketId == p.pocket.id,
                        onClick = {
                            onSelectPocket(
                                if (state.selectedFilterPocketId == p.pocket.id) null else p.pocket.id
                            )
                        },
                        label = { Text(p.pocket.name) }
                    )
                }
            }
        }

        // Summary Strip
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.filteredHistoryIncomes.size} catatan ditemukan",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatRupiah(filteredSum),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (hasActiveFilters) {
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = onClearFilters) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }

        if (grouped.isEmpty()) {
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
                            contentDescription = "Tidak ada riwayat",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp, 100.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Text(
                            text = if (hasActiveFilters) "Tidak ada catatan yang cocok" else "Belum ada riwayat pemasukan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (hasActiveFilters) {
                                "Coba ubah kata kunci pencarian atau reset filter."
                            } else {
                                "Ketuk tombol + Catat Pemasukan untuk mulai mencatat."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            grouped.forEach { (dayStartMillis, entries) ->
                val daySubtotal = entries.sumOf { it.income.amount }
                item(key = "header_$dayStartMillis") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateGroupLabel(dayStartMillis),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRupiah(daySubtotal),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(entries, key = { it.income.id }) { item ->
                    IncomeRowCard(
                        item = item,
                        onClick = { onEditIncomeClick(item.income) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
