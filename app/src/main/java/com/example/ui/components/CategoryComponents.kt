package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.domain.CategorySummary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.KasColorCatalog
import com.example.util.KasIconCatalog
import com.example.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerSheet(
    categories: List<CategoryEntity>,
    summaries: List<CategorySummary>,
    onDismiss: () -> Unit,
    onCreateCategory: (name: String, iconKey: String, colorHex: Long) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDeleteCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    val summaryMap = remember(summaries) { summaries.associateBy { it.category.id } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kelola Kategori Pemasukan",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Buat, ubah warna & ikon, atau hapus kategori agar grafik mudah dibedakan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    val summary = summaryMap[category.id]
                    val catColor = KasColorCatalog.toColor(category.colorHex)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_row_${category.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.16f))
                                    .border(1.5.dp, catColor, CircleShape)
                                    .clickable { editingCategory = category },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = KasIconCatalog.resolveIcon(category.iconKey),
                                    contentDescription = category.name,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                                val count = summary?.count ?: 0
                                val total = summary?.totalAmount ?: 0L
                                Text(
                                    text = "$count catatan • ${formatRupiah(total)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { editingCategory = category },
                                modifier = Modifier.testTag("edit_category_${category.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Ubah kategori ${category.name}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { pendingDeleteCategory = category },
                                enabled = categories.size > 1,
                                modifier = Modifier.testTag("delete_category_${category.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Hapus kategori ${category.name}",
                                    tint = if (categories.size > 1) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CategoryEditorDialog(
            initialCategory = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, iconKey, colorHex ->
                onCreateCategory(name, iconKey, colorHex)
                showCreateDialog = false
            }
        )
    }

    editingCategory?.let { target ->
        CategoryEditorDialog(
            initialCategory = target,
            onDismiss = { editingCategory = null },
            onSave = { name, iconKey, colorHex ->
                onUpdateCategory(
                    target.copy(
                        name = name,
                        iconKey = iconKey,
                        colorHex = colorHex
                    )
                )
                editingCategory = null
            }
        )
    }

    pendingDeleteCategory?.let { target ->
        val count = summaryMap[target.id]?.count ?: 0
        AlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text("Hapus Kategori?") },
            text = {
                Text(
                    if (count > 0) {
                        "Kategori \"${target.name}\" memiliki $count catatan pemasukan. Catatan tersebut akan dipindahkan otomatis ke kategori cadangan agar data tidak hilang."
                    } else {
                        "Kategori \"${target.name}\" akan dihapus secara permanen."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(target)
                        pendingDeleteCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_category_button")
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    initialCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, colorHex: Long) -> Unit
) {
    var name by remember(initialCategory) { mutableStateOf(initialCategory?.name ?: "") }
    var selectedIconKey by remember(initialCategory) {
        mutableStateOf(initialCategory?.iconKey ?: KasIconCatalog.categoryIcons.first().key)
    }
    var selectedColorHex by remember(initialCategory) {
        mutableLongStateOf(initialCategory?.colorHex ?: KasColorCatalog.swatches.first())
    }
    val initialHsv = remember(initialCategory) {
        KasColorCatalog.colorLongToHsv(initialCategory?.colorHex ?: KasColorCatalog.swatches.first())
    }
    var hue by remember(initialCategory) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialCategory) { mutableFloatStateOf(initialHsv[1].coerceAtLeast(0.25f)) }
    var brightness by remember(initialCategory) { mutableFloatStateOf(initialHsv[2].coerceAtLeast(0.35f)) }
    var showCustomColorPicker by remember(initialCategory) {
        mutableStateOf(
            initialCategory != null && !KasColorCatalog.swatches.contains(initialCategory.colorHex)
        )
    }
    var showError by remember { mutableStateOf(false) }

    val previewColor = KasColorCatalog.toColor(selectedColorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCategory == null) "Kategori Baru" else "Edit Kategori",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live preview chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = previewColor.copy(alpha = 0.14f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(previewColor)
                        )
                        Icon(
                            imageVector = KasIconCatalog.resolveIcon(selectedIconKey),
                            contentDescription = null,
                            tint = previewColor
                        )
                        Text(
                            text = name.ifBlank { "Nama Kategori" },
                            style = MaterialTheme.typography.titleSmall,
                            color = previewColor
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 28) {
                            name = it
                            showError = false
                        }
                    },
                    label = { Text("Nama Kategori") },
                    placeholder = { Text("Contoh: Jualan Online, Komisi") },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Nama kategori wajib diisi") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Text(
                    text = "Pilih Ikon",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KasIconCatalog.categoryIcons.forEach { option ->
                        val isSelected = option.key == selectedIconKey
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) previewColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) previewColor else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIconKey = option.key }
                                .testTag("icon_option_${option.key}"),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Warna Kategori",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = KasColorCatalog.toHexString(selectedColorHex),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = previewColor
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KasColorCatalog.swatches.forEach { hex ->
                        val swatchColor = KasColorCatalog.toColor(hex)
                        val isSelected = hex == selectedColorHex && !showCustomColorPicker
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
                                .clickable {
                                    selectedColorHex = hex
                                    val hsv = KasColorCatalog.colorLongToHsv(hex)
                                    hue = hsv[0]
                                    saturation = hsv[1].coerceAtLeast(0.2f)
                                    brightness = hsv[2].coerceAtLeast(0.25f)
                                    showCustomColorPicker = false
                                }
                                .testTag("color_swatch_${KasColorCatalog.toHexString(hex)}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Warna terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Custom color picker toggle button
                    Surface(
                        shape = CircleShape,
                        color = if (showCustomColorPicker) {
                            previewColor.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .height(34.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (showCustomColorPicker) 2.dp else 0.dp,
                                color = if (showCustomColorPicker) previewColor else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { showCustomColorPicker = !showCustomColorPicker }
                            .testTag("toggle_custom_color_picker")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = "Pilih warna kustom",
                                tint = if (showCustomColorPicker) previewColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Kustom",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (showCustomColorPicker) previewColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showCustomColorPicker) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_hsv_picker_panel")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Spektrum Warna (Hue)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Red,
                                                Color.Yellow,
                                                Color.Green,
                                                Color.Cyan,
                                                Color.Blue,
                                                Color.Magenta,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                            Slider(
                                value = hue,
                                onValueChange = {
                                    hue = it
                                    selectedColorHex = KasColorCatalog.hsvToColorLong(hue, saturation, brightness)
                                },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(
                                    thumbColor = previewColor,
                                    activeTrackColor = previewColor
                                ),
                                modifier = Modifier.testTag("custom_hue_slider")
                            )

                            Text(
                                text = "Saturasi (${(saturation * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = saturation,
                                onValueChange = {
                                    saturation = it
                                    selectedColorHex = KasColorCatalog.hsvToColorLong(hue, saturation, brightness)
                                },
                                valueRange = 0.2f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = previewColor,
                                    activeTrackColor = previewColor
                                ),
                                modifier = Modifier.testTag("custom_saturation_slider")
                            )

                            Text(
                                text = "Kecerahan (${(brightness * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = brightness,
                                onValueChange = {
                                    brightness = it
                                    selectedColorHex = KasColorCatalog.hsvToColorLong(hue, saturation, brightness)
                                },
                                valueRange = 0.3f..0.95f,
                                colors = SliderDefaults.colors(
                                    thumbColor = previewColor,
                                    activeTrackColor = previewColor
                                ),
                                modifier = Modifier.testTag("custom_brightness_slider")
                            )
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
                        onSave(name.trim(), selectedIconKey, selectedColorHex)
                    }
                },
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
