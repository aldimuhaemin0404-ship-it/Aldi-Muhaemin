package com.example.domain

import com.example.data.local.CategoryEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.PocketEntity
import com.example.data.local.PocketTransferEntity
import kotlin.math.ceil

data class IncomeWithDetails(
    val income: IncomeEntity,
    val category: CategoryEntity?,
    val pocket: PocketEntity?
)

data class PocketWithBalance(
    val pocket: PocketEntity,
    val balance: Long,
    val todayIncome: Long,
    val entryCount: Int,
    val remainingToTarget: Long = (pocket.targetAmount - balance).coerceAtLeast(0L),
    val estimatedDaysRemaining: Int? = null
)

data class SavingsGoalProgress(
    val goalName: String = "Target Tabungan Utama",
    val targetAmount: Long = 5_000_000L,
    val savedAmount: Long = 0L,
    val remainingAmount: Long = 5_000_000L,
    val progressFraction: Float = 0f,
    val percentageInt: Int = 0,
    val isAchieved: Boolean = false,
    val effectiveDailyAverage: Long = 0L,
    val estimatedDaysRemaining: Int? = null
) {
    companion object {
        fun calculate(
            goalName: String,
            targetAmount: Long,
            totalIncome: Long,
            effectiveDailyAverage: Long
        ): SavingsGoalProgress {
            val safeTarget = targetAmount.coerceAtLeast(0L)
            val safeSaved = totalIncome.coerceAtLeast(0L)
            val remaining = (safeTarget - safeSaved).coerceAtLeast(0L)
            val fraction = if (safeTarget > 0L) {
                (safeSaved.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            val pct = if (safeTarget > 0L) {
                ((safeSaved.toDouble() / safeTarget.toDouble()) * 100.0).toInt()
            } else {
                0
            }
            val achieved = safeTarget > 0L && safeSaved >= safeTarget
            val estDays = when {
                achieved -> 0
                safeTarget > 0L && effectiveDailyAverage > 0L ->
                    ceil(remaining.toDouble() / effectiveDailyAverage.toDouble()).toInt()
                else -> null
            }
            return SavingsGoalProgress(
                goalName = goalName,
                targetAmount = safeTarget,
                savedAmount = safeSaved,
                remainingAmount = remaining,
                progressFraction = fraction,
                percentageInt = pct,
                isAchieved = achieved,
                effectiveDailyAverage = effectiveDailyAverage,
                estimatedDaysRemaining = estDays
            )
        }
    }
}

data class DailyBarPoint(
    val dateMillis: Long,
    val dayShort: String,
    val dateLabel: String,
    val totalAmount: Long,
    val isToday: Boolean
)

data class TrendLinePoint(
    val label: String,
    val detailLabel: String,
    val totalAmount: Long,
    val entryCount: Int
)

data class CategorySummary(
    val category: CategoryEntity,
    val totalAmount: Long,
    val percentage: Float,
    val count: Int
)

enum class TrendPeriod(val label: String) {
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan")
}

enum class HistoryFilterPeriod(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    WEEK("7 Hari"),
    MONTH("Bulan Ini")
}

data class KasUiState(
    val isReady: Boolean = false,
    val totalBalance: Long = 0L,
    val todayTotal: Long = 0L,
    val weekTotal: Long = 0L,
    val monthTotal: Long = 0L,
    val dailyAverageThisMonth: Long = 0L,
    val activeDaysStreak: Int = 0,
    val dailyTarget: Long = 250_000L,
    val savingsGoal: SavingsGoalProgress = SavingsGoalProgress(),
    val categories: List<CategoryEntity> = emptyList(),
    val pockets: List<PocketWithBalance> = emptyList(),
    val allIncomes: List<IncomeWithDetails> = emptyList(),
    val todayIncomes: List<IncomeWithDetails> = emptyList(),
    val filteredHistoryIncomes: List<IncomeWithDetails> = emptyList(),
    val recentTransfers: List<PocketTransferEntity> = emptyList(),
    val weekOffset: Int = 0,
    val barChartRangeLabel: String = "",
    val dailyBarPoints: List<DailyBarPoint> = emptyList(),
    val trendPeriod: TrendPeriod = TrendPeriod.WEEKLY,
    val trendLinePoints: List<TrendLinePoint> = emptyList(),
    val categorySummaries: List<CategorySummary> = emptyList(),
    val searchQuery: String = "",
    val selectedFilterPeriod: HistoryFilterPeriod = HistoryFilterPeriod.ALL,
    val selectedFilterCategoryId: Long? = null,
    val selectedFilterPocketId: Long? = null
)
