package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.SavingsGoalProgress
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.formatRupiah

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalProgress,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "savingsGoalProgress"
    )
    val accentColor = if (goal.isAchieved) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onEditGoalClick)
            .testTag("savings_goal_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (goal.isAchieved) Icons.Rounded.EmojiEvents else Icons.Rounded.Flag,
                            contentDescription = goal.goalName,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.goalName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Progres dari total pemasukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onEditGoalClick,
                    modifier = Modifier.testTag("edit_savings_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Atur Target")
                }
            }

            // Progress bar & percentage badge
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = formatRupiah(goal.savedAmount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = accentColor
                        )
                        Text(
                            text = "dari target ${formatRupiah(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (goal.isAchieved) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = if (goal.isAchieved) {
                                "${goal.percentageInt}% Tercapai!"
                            } else {
                                "${goal.percentageInt}%"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (goal.isAchieved) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("savings_goal_percentage_text")
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            // Remaining amount & Estimated days breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (goal.isAchieved) "Surplus Target" else "Sisa Perlu Ditabung",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (goal.isAchieved) {
                                "+${formatRupiah((goal.savedAmount - goal.targetAmount).coerceAtLeast(0L))}"
                            } else {
                                formatRupiah(goal.remainingAmount)
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (goal.isAchieved) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("savings_goal_remaining_text")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Estimasi Tercapai",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = when {
                                goal.isAchieved -> "Target terpenuhi"
                                goal.estimatedDaysRemaining != null -> "~${goal.estimatedDaysRemaining} hari lagi"
                                else -> "Belum tersedia"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (goal.isAchieved) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("savings_goal_eta_text")
                        )
                        if (!goal.isAchieved && goal.effectiveDailyAverage > 0L) {
                            Text(
                                text = "Rata-rata ${formatRupiah(goal.effectiveDailyAverage)}/hr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsGoalDialog(
    currentName: String,
    currentTarget: Long,
    onDismiss: () -> Unit,
    onSave: (goalName: String, targetAmount: Long) -> Unit
) {
    var goalName by remember(currentName) { mutableStateOf(currentName) }
    var targetText by remember(currentTarget) {
        mutableStateOf(if (currentTarget > 0L) currentTarget.toString() else "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedAmount = targetText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Flag, contentDescription = null) },
        title = { Text("Atur Target Tabungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Progres dan sisa nominal yang perlu ditabung akan dihitung otomatis dari total pemasukan Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = goalName,
                    onValueChange = {
                        if (it.length <= 40) {
                            goalName = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Nama Tujuan Tabungan") },
                    placeholder = { Text("Contoh: Dana Darurat, Modal Usaha") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_goal_name_input")
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it.filter { ch -> ch.isDigit() }.take(12)
                        errorMessage = null
                    },
                    label = { Text("Nominal Target (Rp)") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (parsedAmount > 0L) {
                            Text(
                                text = formatRupiah(parsedAmount),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_goal_amount_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1_000_000L to "1 jt",
                        2_500_000L to "2,5 jt",
                        5_000_000L to "5 jt",
                        10_000_000L to "10 jt",
                        25_000_000L to "25 jt"
                    ).forEach { (preset, label) ->
                        AssistChip(
                            onClick = {
                                targetText = preset.toString()
                                errorMessage = null
                            },
                            label = { Text(label) },
                            modifier = Modifier.testTag("preset_goal_$preset")
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        goalName.isBlank() -> errorMessage = "Nama tujuan tabungan wajib diisi."
                        parsedAmount <= 0L -> errorMessage = "Nominal target harus lebih dari Rp 0."
                        else -> onSave(goalName.trim(), parsedAmount)
                    }
                },
                modifier = Modifier.testTag("save_savings_goal_button")
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
