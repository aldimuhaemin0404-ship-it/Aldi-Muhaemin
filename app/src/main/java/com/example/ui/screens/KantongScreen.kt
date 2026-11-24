package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PocketEntity
import com.example.domain.KasUiState
import com.example.ui.components.PocketEditorDialog
import com.example.ui.components.TransferPocketDialog
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatRupiah
import com.example.util.formatShortDate

@Composable
fun KantongScreen(
    state: KasUiState,
    onEditSavingsGoalClick: () -> Unit,
    onCreatePocket: (name: String, iconKey: String, colorHex: Long, targetAmount: Long) -> Unit,
    onUpdatePocket: (PocketEntity) -> Unit,
    onDeletePocket: (PocketEntity) -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Long, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPocket by remember { mutableStateOf<PocketEntity?>(null) }
    var pendingDeletePocket by remember { mutableStateOf<PocketEntity?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val pocketNameMap = remember(state.pockets) {
        state.pockets.associate { it.pocket.id to it.pocket.name }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("kantong_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Kantong & Target Tabungan",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Atur target tabungan utama, kantong simpanan, & target tujuan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            com.example.ui.components.SavingsGoalCard(
                goal = state.savingsGoal,
                onEditGoalClick = onEditSavingsGoalClick
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Total Simpanan di Semua Kantong",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatRupiah(state.totalBalance),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_pocket_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kantong Baru")
                        }
                        FilledTonalButton(
                            onClick = { showTransferDialog = true },
                            enabled = state.pockets.size >= 2,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_transfer_button")
                        ) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pindah Saldo")
                        }
                    }
                }
            }
        }

        items(state.pockets, key = { it.pocket.id }) { item ->
            val pColor = KasColorCatalog.toColor(item.pocket.colorHex)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pocket_card_${item.pocket.id}"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = KasIconCatalog.resolveIcon(item.pocket.iconKey),
                                contentDescription = item.pocket.name,
                                tint = pColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.pocket.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${item.entryCount} pemasukan tercatat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { editingPocket = item.pocket }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Ubah ${item.pocket.name}",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { pendingDeletePocket = item.pocket },
                            enabled = state.pockets.size > 1
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Hapus ${item.pocket.name}",
                                tint = if (state.pockets.size > 1) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = formatRupiah(item.balance),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = pColor
                        )
                        if (item.todayIncome > 0L) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "+${formatRupiah(item.todayIncome)} hari ini",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (item.pocket.targetAmount > 0L) {
                        val progress = (item.balance.toFloat() / item.pocket.targetAmount.toFloat()).coerceIn(0f, 1f)
                        val pct = ((item.balance.toFloat() / item.pocket.targetAmount.toFloat()) * 100).toInt()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = pColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (item.remainingToTarget == 0L) {
                                        "Target Tercapai ($pct%)"
                                    } else {
                                        "Terkumpul $pct% • Sisa ${formatRupiah(item.remainingToTarget)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Target ${formatRupiah(item.pocket.targetAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.remainingToTarget > 0L && item.estimatedDaysRemaining != null) {
                                Text(
                                    text = "Estimasi tercapai: ~${item.estimatedDaysRemaining} hari lagi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.recentTransfers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Riwayat Pemindahan Saldo",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(state.recentTransfers, key = { it.id }) { transfer ->
                val fromName = pocketNameMap[transfer.fromPocketId] ?: "Kantong"
                val toName = pocketNameMap[transfer.toPocketId] ?: "Kantong"
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$fromName → $toName",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = buildString {
                                    append(formatShortDate(transfer.dateMillis))
                                    if (transfer.note.isNotBlank()) append(" • ${transfer.note}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatRupiah(transfer.amount),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showCreateDialog) {
        PocketEditorDialog(
            initialPocket = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, iconKey, colorHex, target ->
                onCreatePocket(name, iconKey, colorHex, target)
                showCreateDialog = false
            }
        )
    }

    editingPocket?.let { target ->
        PocketEditorDialog(
            initialPocket = target,
            onDismiss = { editingPocket = null },
            onSave = { name, iconKey, colorHex, targetAmt ->
                onUpdatePocket(
                    target.copy(
                        name = name,
                        iconKey = iconKey,
                        colorHex = colorHex,
                        targetAmount = targetAmt
                    )
                )
                editingPocket = null
            }
        )
    }

    pendingDeletePocket?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeletePocket = null },
            title = { Text("Hapus Kantong?") },
            text = {
                Text("Catatan pemasukan di kantong \"${target.name}\" akan dipindahkan ke kantong lain yang tersedia.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePocket(target)
                        pendingDeletePocket = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePocket = null }) { Text("Batal") }
            }
        )
    }

    if (showTransferDialog) {
        TransferPocketDialog(
            pockets = state.pockets,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromId, toId, amount, note ->
                onTransfer(fromId, toId, amount, note)
                showTransferDialog = false
            }
        )
    }
}
