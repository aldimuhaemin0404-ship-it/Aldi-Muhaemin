package com.example.data.repository

import com.example.data.local.AppSettingsEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.KasHarianDao
import com.example.data.local.PocketEntity
import com.example.data.local.PocketTransferEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KasHarianRepository(private val dao: KasHarianDao) {

    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val pockets: Flow<List<PocketEntity>> = dao.observePockets()
    val incomes: Flow<List<IncomeEntity>> = dao.observeIncomes()
    val transfers: Flow<List<PocketTransferEntity>> = dao.observeTransfers()
    val settings: Flow<AppSettingsEntity> = dao.observeSettings().map { it ?: AppSettingsEntity() }
    val dailyTarget: Flow<Long> = settings.map { it.dailyTarget }

    suspend fun ensureDefaultsInitialized() {
        if (dao.getCategoryCount() == 0) {
            dao.insertCategories(
                listOf(
                    CategoryEntity(
                        name = "Usaha & Jualan",
                        iconKey = "storefront",
                        colorHex = 0xFF0D6E4F,
                        isDefault = true
                    ),
                    CategoryEntity(
                        name = "Gaji",
                        iconKey = "work",
                        colorHex = 0xFF0284C7,
                        isDefault = true
                    ),
                    CategoryEntity(
                        name = "Freelance",
                        iconKey = "laptop",
                        colorHex = 0xFF7C3AED,
                        isDefault = true
                    ),
                    CategoryEntity(
                        name = "Bonus",
                        iconKey = "card_giftcard",
                        colorHex = 0xFFD97706,
                        isDefault = true
                    ),
                    CategoryEntity(
                        name = "Investasi",
                        iconKey = "trending_up",
                        colorHex = 0xFF059669,
                        isDefault = true
                    ),
                    CategoryEntity(
                        name = "Lainnya",
                        iconKey = "payments",
                        colorHex = 0xFF64748B,
                        isDefault = true
                    )
                )
            )
        }
        if (dao.getPocketCount() == 0) {
            dao.insertPockets(
                listOf(
                    PocketEntity(
                        name = "Dompet Tunai",
                        iconKey = "wallet",
                        colorHex = 0xFF0D6E4F,
                        targetAmount = 0L
                    ),
                    PocketEntity(
                        name = "Rekening Bank",
                        iconKey = "account_balance",
                        colorHex = 0xFF0284C7,
                        targetAmount = 0L
                    ),
                    PocketEntity(
                        name = "Tabungan Darurat",
                        iconKey = "savings",
                        colorHex = 0xFFD97706,
                        targetAmount = 5_000_000L
                    )
                )
            )
        }
        if (dao.getSettingsCount() == 0) {
            dao.upsertSettings(
                AppSettingsEntity(
                    id = 1,
                    dailyTarget = 250_000L,
                    savingsGoalName = "Target Tabungan Utama",
                    savingsTarget = 5_000_000L
                )
            )
        }
    }

    suspend fun addIncome(
        title: String,
        amount: Long,
        categoryId: Long,
        pocketId: Long,
        dateMillis: Long,
        note: String
    ): Long = dao.insertIncome(
        IncomeEntity(
            title = title.trim(),
            amount = amount,
            categoryId = categoryId,
            pocketId = pocketId,
            dateMillis = dateMillis,
            note = note.trim()
        )
    )

    suspend fun updateIncome(income: IncomeEntity) {
        dao.updateIncome(income)
    }

    suspend fun deleteIncome(incomeId: Long) {
        dao.deleteIncomeById(incomeId)
    }

    suspend fun createCategory(name: String, iconKey: String, colorHex: Long): Long =
        dao.insertCategory(
            CategoryEntity(
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                isDefault = false
            )
        )

    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category.copy(name = category.name.trim()))
    }

    suspend fun deleteCategory(categoryId: Long, fallbackCategoryId: Long) {
        dao.deleteCategoryWithReassign(categoryId, fallbackCategoryId)
    }

    suspend fun createPocket(name: String, iconKey: String, colorHex: Long, targetAmount: Long): Long =
        dao.insertPocket(
            PocketEntity(
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                targetAmount = targetAmount
            )
        )

    suspend fun updatePocket(pocket: PocketEntity) {
        dao.updatePocket(pocket.copy(name = pocket.name.trim()))
    }

    suspend fun deletePocket(pocketId: Long, fallbackPocketId: Long) {
        dao.deletePocketWithReassign(pocketId, fallbackPocketId)
    }

    suspend fun transferBetweenPockets(
        fromPocketId: Long,
        toPocketId: Long,
        amount: Long,
        note: String
    ) {
        dao.insertTransfer(
            PocketTransferEntity(
                fromPocketId = fromPocketId,
                toPocketId = toPocketId,
                amount = amount,
                note = note.trim(),
                dateMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateDailyTarget(target: Long) {
        if (dao.getSettingsCount() == 0) {
            dao.upsertSettings(AppSettingsEntity(id = 1, dailyTarget = target))
        } else {
            dao.updateDailyTargetOnly(target)
        }
    }

    suspend fun updateSavingsGoal(goalName: String, targetAmount: Long) {
        val cleanName = goalName.trim().ifBlank { "Target Tabungan Utama" }
        if (dao.getSettingsCount() == 0) {
            dao.upsertSettings(
                AppSettingsEntity(
                    id = 1,
                    savingsGoalName = cleanName,
                    savingsTarget = targetAmount
                )
            )
        } else {
            dao.updateSavingsGoalOnly(cleanName, targetAmount)
        }
    }
}
