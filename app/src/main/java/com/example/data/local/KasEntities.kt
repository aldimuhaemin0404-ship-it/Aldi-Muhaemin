package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pockets")
data class PocketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val targetAmount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Long,
    val categoryId: Long,
    val pocketId: Long,
    val dateMillis: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pocket_transfers")
data class PocketTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fromPocketId: Long,
    val toPocketId: Long,
    val amount: Long,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyTarget: Long = 250_000L,
    val savingsGoalName: String = "Target Tabungan Utama",
    val savingsTarget: Long = 5_000_000L
)
