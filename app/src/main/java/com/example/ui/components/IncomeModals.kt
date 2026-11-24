package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.PocketEntity
import com.example.domain.IncomeWithDetails
import com.example.domain.PocketWithBalance
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatRupiah
import com.example.util.formatShortDate

@Composable
fun IncomeRowCard(
    item: IncomeWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = KasColorCatalog.toColor(item.category?.colorHex ?: 0xFF0D6E4F)
    val categoryIcon = KasIconCatalog.resolveIcon(item.category?.iconKey ?: "payments")
    val categoryName = item.category?.name ?: "Lainnya"
    val pocketName = item.pocket?.name ?: "Dompet"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("income_item_${item.income.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = categoryName,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.income.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "• $pocketName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.income.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.income.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "+${formatRupiah(item.income.amount)}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditIncomeSheet(
    initialIncome: IncomeEntity?,
    categories: List<CategoryEntity>,
    pockets: List<PocketEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Long, categoryId: Long, pocketId: Long, dateMillis: Long, note: String) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onQuickCreateCategory: (name: String, iconKey: String, colorHex: Long, onCreated: (Long) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember(initialIncome) {
        mutableStateOf(initialIncome?.amount?.toString() ?: "")
    }
    var title by remember(initialIncome) { mutableStateOf(initialIncome?.title ?: "") }
    var selectedCategoryId by remember(initialIncome, categories) {
        mutableLongStateOf(
            initialIncome?.categoryId ?: categories.firstOrNull()?.id ?: 1L
        )
    }
    var selectedPocketId by remember(initialIncome, pockets) {
        mutableLongStateOf(
            initialIncome?.pocketId ?: pockets.firstOrNull()?.id ?: 1L
        )
    }
    var selectedDateMillis by remember(initialIncome) {
        mutableLongStateOf(initialIncome?.dateMillis ?: System.currentTimeMillis())
    }
    var note by remember(initialIncome) { mutableStateOf(initialIncome?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showQuickCategoryDialog by remember { mutableStateOf(false) }

    val parsedAmount = amountText.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (initialIncome == null) "Catat Pemasukan Harian" else "Edit Pemasukan",
                style = MaterialTheme.typography.titleLarge
            )

            // Amount Input + Live Preview
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(12)
                    amountText = digits
                    errorMessage = null
                },
                label = { Text("Nominal Pemasukan (Rp)") },
                placeholder = { Text("Contoh: 150000") },
                prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                supportingText = {
                    if (parsedAmount > 0L) {
                        Text(
                            text = formatRupiah(parsedAmount),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("income_amount_input")
            )

            // Quick amount add chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    10_000L to "+10rb",
                    25_000L to "+25rb",
                    50_000L to "+50rb",
                    100_000L to "+100rb",
                    500_000L to "+500rb"
                ).forEach { (delta, label) ->
                    AssistChip(
                        onClick = {
                            val next = (parsedAmount + delta).coerceAtMost(999_999_999_999L)
                            amountText = next.toString()
                            errorMessage = null
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("quick_amount_$delta")
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    if (it.length <= 60) {
                        title = it
                        errorMessage = null
                    }
                },
                label = { Text("Judul / Sumber Pemasukan") },
                placeholder = { Text("Contoh: Penjualan Hari Ini, Jasa Desain") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("income_title_input")
            )

            // Date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tanggal Pemasukan",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.testTag("income_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatShortDate(selectedDateMillis))
                }
            }

            // Category selection + Quick create custom category
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kategori Pemasukan",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val catColor = KasColorCatalog.toColor(category.colorHex)
                        val isSelected = category.id == selectedCategoryId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = category.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = KasIconCatalog.resolveIcon(category.iconKey),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else catColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("select_category_${category.id}")
                        )
                    }
                    AssistChip(
                        onClick = { showQuickCategoryDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Kategori Baru") },
                        modifier = Modifier.testTag("quick_add_category_chip")
                    )
                }
            }

            // Storage Pocket selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Simpan ke Kantong",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pockets.forEach { pocket ->
                        val pColor = KasColorCatalog.toColor(pocket.colorHex)
                        val isSelected = pocket.id == selectedPocketId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPocketId = pocket.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = KasIconCatalog.resolveIcon(pocket.iconKey),
                                    contentDescription = null,
                                    tint = pColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text(pocket.name) },
                            modifier = Modifier.testTag("select_pocket_${pocket.id}")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 140) note = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                placeholder = { Text("Misal: Pembayaran lunas via transfer") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("income_note_input")
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (initialIncome != null && onDelete != null) {
                    OutlinedButton(
                        onClick = { onDelete(initialIncome.id) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("delete_income_button")
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus")
                    }
                }

                Button(
                    onClick = {
                        when {
                            parsedAmount <= 0L -> errorMessage = "Masukkan nominal pemasukan lebih dari Rp 0."
                            title.isBlank() -> errorMessage = "Judul sumber pemasukan wajib diisi."
                            else -> onSave(
                                title.trim(),
                                parsedAmount,
                                selectedCategoryId,
                                selectedPocketId,
                                selectedDateMillis,
                                note.trim()
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_income_button")
                ) {
                    Text(if (initialIncome == null) "Simpan Pemasukan" else "Perbarui")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showQuickCategoryDialog) {
        CategoryEditorDialog(
            initialCategory = null,
            onDismiss = { showQuickCategoryDialog = false },
            onSave = { name, iconKey, colorHex ->
                onQuickCreateCategory(name, iconKey, colorHex) { newId ->
                    selectedCategoryId = newId
                }
                showQuickCategoryDialog = false
            }
        )
    }
}

@Composable
fun DailyTargetDialog(
    currentTarget: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var targetText by remember(currentTarget) { mutableStateOf(currentTarget.toString()) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Target Pemasukan Harian") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Atur target pemasukan harian Anda untuk memantau pencapaian pada grafik dan beranda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it.filter { ch -> ch.isDigit() }.take(12)
                        showError = false
                    },
                    label = { Text("Target Harian (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = showError,
                    supportingText = {
                        val parsed = targetText.toLongOrNull() ?: 0L
                        if (showError) {
                            Text("Target harus lebih dari Rp 0")
                        } else if (parsed > 0L) {
                            Text(formatRupiah(parsed))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_target_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = targetText.toLongOrNull() ?: 0L
                    if (value <= 0L) showError = true else onSave(value)
                },
                modifier = Modifier.testTag("save_daily_target_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PocketEditorDialog(
    initialPocket: PocketEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, colorHex: Long, targetAmount: Long) -> Unit
) {
    var name by remember(initialPocket) { mutableStateOf(initialPocket?.name ?: "") }
    var targetText by remember(initialPocket) {
        mutableStateOf(
            if ((initialPocket?.targetAmount ?: 0L) > 0L) initialPocket?.targetAmount.toString() else ""
        )
    }
    var selectedIconKey by remember(initialPocket) {
        mutableStateOf(initialPocket?.iconKey ?: KasIconCatalog.pocketIcons.first().key)
    }
    var selectedColorHex by remember(initialPocket) {
        mutableLongStateOf(initialPocket?.colorHex ?: KasColorCatalog.swatches.first())
    }
    var showError by remember { mutableStateOf(false) }

    val previewColor = KasColorCatalog.toColor(selectedColorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialPocket == null) "Kantong Simpanan Baru" else "Edit Kantong Simpanan")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 28) {
                            name = it
                            showError = false
                        }
                    },
                    label = { Text("Nama Kantong") },
                    placeholder = { Text("Contoh: Tabungan Umroh, Modal Usaha") },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Nama kantong wajib diisi") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pocket_name_input")
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it.filter { ch -> ch.isDigit() }.take(12)
                    },
                    label = { Text("Target Tabungan (Opsional)") },
                    placeholder = { Text("Kosongkan jika tanpa target") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        val parsed = targetText.toLongOrNull() ?: 0L
                        if (parsed > 0L) Text(formatRupiah(parsed))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pocket_target_input")
                )

                Text("Ikon", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KasIconCatalog.pocketIcons.forEach { option ->
                        val isSelected = option.key == selectedIconKey
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) previewColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) previewColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIconKey = option.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = if (isSelected) previewColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text("Warna", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KasColorCatalog.swatches.forEach { hex ->
                        val swatchColor = KasColorCatalog.toColor(hex)
                        val isSelected = hex == selectedColorHex
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onSave(
                            name.trim(),
                            selectedIconKey,
                            selectedColorHex,
                            targetText.toLongOrNull() ?: 0L
                        )
                    }
                },
                modifier = Modifier.testTag("save_pocket_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransferPocketDialog(
    pockets: List<PocketWithBalance>,
    onDismiss: () -> Unit,
    onTransfer: (fromPocketId: Long, toPocketId: Long, amount: Long, note: String) -> Unit
) {
    var fromId by remember(pockets) { mutableLongStateOf(pockets.firstOrNull()?.pocket?.id ?: 0L) }
    var toId by remember(pockets) {
        mutableLongStateOf(
            pockets.getOrNull(1)?.pocket?.id ?: pockets.firstOrNull()?.pocket?.id ?: 0L
        )
    }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val fromPocket = pockets.firstOrNull { it.pocket.id == fromId }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
        title = { Text("Pindah Saldo Antar Kantong") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Dari Kantong", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pockets.forEach { item ->
                        FilterChip(
                            selected = item.pocket.id == fromId,
                            onClick = {
                                fromId = item.pocket.id
                                errorMsg = null
                            },
                            label = { Text("${item.pocket.name} (${formatRupiah(item.balance)})") }
                        )
                    }
                }

                Text("Ke Kantong", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pockets.forEach { item ->
                        FilterChip(
                            selected = item.pocket.id == toId,
                            onClick = {
                                toId = item.pocket.id
                                errorMsg = null
                            },
                            label = { Text(item.pocket.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }.take(12)
                        errorMsg = null
                    },
                    label = { Text("Nominal Pemindahan") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 80) note = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    when {
                        fromId == toId -> errorMsg = "Pilih kantong tujuan yang berbeda."
                        amount <= 0L -> errorMsg = "Nominal harus lebih dari Rp 0."
                        fromPocket != null && amount > fromPocket.balance ->
                            errorMsg = "Saldo kantong sumber tidak mencukupi."
                        else -> onTransfer(fromId, toId, amount, note.trim())
                    }
                },
                modifier = Modifier.testTag("confirm_transfer_button")
            ) {
                Text("Pindahkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
