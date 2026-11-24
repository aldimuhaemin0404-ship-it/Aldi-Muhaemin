package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KasHarianDao {

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, createdAt ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE incomes SET categoryId = :fallbackCategoryId WHERE categoryId = :deletedCategoryId")
    suspend fun reassignIncomeCategory(deletedCategoryId: Long, fallbackCategoryId: Long)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Transaction
    suspend fun deleteCategoryWithReassign(categoryId: Long, fallbackCategoryId: Long) {
        reassignIncomeCategory(categoryId, fallbackCategoryId)
        deleteCategoryById(categoryId)
    }

    @Query("SELECT * FROM pockets ORDER BY createdAt ASC")
    fun observePockets(): Flow<List<PocketEntity>>

    @Query("SELECT COUNT(*) FROM pockets")
    suspend fun getPocketCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: PocketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPockets(pockets: List<PocketEntity>)

    @Update
    suspend fun updatePocket(pocket: PocketEntity)

    @Query("UPDATE incomes SET pocketId = :fallbackPocketId WHERE pocketId = :deletedPocketId")
    suspend fun reassignIncomePocket(deletedPocketId: Long, fallbackPocketId: Long)

    @Query("DELETE FROM pocket_transfers WHERE fromPocketId = :pocketId OR toPocketId = :pocketId")
    suspend fun deleteTransfersForPocket(pocketId: Long)

    @Query("DELETE FROM pockets WHERE id = :pocketId")
    suspend fun deletePocketById(pocketId: Long)

    @Transaction
    suspend fun deletePocketWithReassign(pocketId: Long, fallbackPocketId: Long) {
        reassignIncomePocket(pocketId, fallbackPocketId)
        deleteTransfersForPocket(pocketId)
        deletePocketById(pocketId)
    }

    @Query("SELECT * FROM incomes ORDER BY dateMillis DESC, createdAt DESC")
    fun observeIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :incomeId")
    suspend fun deleteIncomeById(incomeId: Long)

    @Query("SELECT * FROM pocket_transfers ORDER BY dateMillis DESC, createdAt DESC")
    fun observeTransfers(): Flow<List<PocketTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: PocketTransferEntity): Long

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT COUNT(*) FROM app_settings")
    suspend fun getSettingsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET dailyTarget = :dailyTarget WHERE id = 1")
    suspend fun updateDailyTargetOnly(dailyTarget: Long)

    @Query("UPDATE app_settings SET savingsGoalName = :goalName, savingsTarget = :savingsTarget WHERE id = 1")
    suspend fun updateSavingsGoalOnly(goalName: String, savingsTarget: Long)
}
