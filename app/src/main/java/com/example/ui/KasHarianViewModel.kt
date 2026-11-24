package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.PocketEntity
import com.example.data.repository.KasHarianRepository
import com.example.domain.CategorySummary
import com.example.domain.DailyBarPoint
import com.example.domain.HistoryFilterPeriod
import com.example.domain.IncomeWithDetails
import com.example.domain.KasUiState
import com.example.domain.PocketWithBalance
import com.example.domain.TrendLinePoint
import com.example.domain.TrendPeriod
import com.example.util.endOfDayMillis
import com.example.util.formatDayMonth
import com.example.util.formatDayShort
import com.example.util.formatMonthShort
import com.example.util.formatMonthYear
import com.example.util.startOfDayMillis
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private data class UiControlState(
    val weekOffset: Int = 0,
    val trendPeriod: TrendPeriod = TrendPeriod.WEEKLY,
    val searchQuery: String = "",
    val filterPeriod: HistoryFilterPeriod = HistoryFilterPeriod.ALL,
    val filterCategoryId: Long? = null,
    val filterPocketId: Long? = null
)

class KasHarianViewModel(
    private val repository: KasHarianRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(UiControlState())

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultsInitialized()
        }
    }

    val uiState: StateFlow<KasUiState> = combine(
        repository.categories,
        repository.pockets,
        repository.incomes,
        repository.transfers,
        repository.settings,
        controlState
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val categories = values[0] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val pockets = values[1] as List<PocketEntity>
        @Suppress("UNCHECKED_CAST")
        val incomes = values[2] as List<IncomeEntity>
        @Suppress("UNCHECKED_CAST")
        val transfers = values[3] as List<com.example.data.local.PocketTransferEntity>
        val settings = values[4] as com.example.data.local.AppSettingsEntity
        val ctrl = values[5] as UiControlState

        buildUiState(categories, pockets, incomes, transfers, settings, ctrl)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = KasUiState()
    )

    private fun buildUiState(
        categories: List<CategoryEntity>,
        pockets: List<PocketEntity>,
        incomes: List<IncomeEntity>,
        transfers: List<com.example.data.local.PocketTransferEntity>,
        settings: com.example.data.local.AppSettingsEntity,
        ctrl: UiControlState
    ): KasUiState {
        val dailyTarget = settings.dailyTarget
        val now = System.currentTimeMillis()
        val todayStart = startOfDayMillis(now)
        val todayEnd = endOfDayMillis(now)
        val weekStart = todayStart - 6 * DAY_MS

        val monthCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = monthCal.timeInMillis
        val daysElapsedInMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)

        val categoryMap = categories.associateBy { it.id }
        val pocketMap = pockets.associateBy { it.id }

        val detailedIncomes = incomes.map { inc ->
            IncomeWithDetails(
                income = inc,
                category = categoryMap[inc.categoryId],
                pocket = pocketMap[inc.pocketId]
            )
        }

        val totalBalance = incomes.sumOf { it.amount }
        val todayIncomes = detailedIncomes.filter { it.income.dateMillis in todayStart..todayEnd }
        val todayTotal = todayIncomes.sumOf { it.income.amount }
        val weekTotal = incomes.filter { it.dateMillis in weekStart..todayEnd }.sumOf { it.amount }
        val monthTotal = incomes.filter { it.dateMillis in monthStart..todayEnd }.sumOf { it.amount }
        val dailyAvgMonth = monthTotal / daysElapsedInMonth

        // Active days streak (consecutive days ending today or yesterday with at least 1 income)
        val daysWithIncome = incomes.map { startOfDayMillis(it.dateMillis) }.toHashSet()
        var streak = 0
        var cursorDay = if (daysWithIncome.contains(todayStart)) todayStart else todayStart - DAY_MS
        while (daysWithIncome.contains(cursorDay)) {
            streak++
            cursorDay -= DAY_MS
        }

        // Effective daily average across active recording days for savings goal estimation
        val activeDaysCount = daysWithIncome.size.coerceAtLeast(1)
        val effectiveDailyAverage = if (totalBalance > 0L) totalBalance / activeDaysCount else 0L
        val savingsGoal = com.example.domain.SavingsGoalProgress.calculate(
            goalName = settings.savingsGoalName,
            targetAmount = settings.savingsTarget,
            totalIncome = totalBalance,
            effectiveDailyAverage = effectiveDailyAverage
        )

        // Pocket balances = sum(incomes) + sum(transfers in) - sum(transfers out)
        val pocketsWithBalance = pockets.map { pocket ->
            val pocketIncomes = incomes.filter { it.pocketId == pocket.id }
            val incomeSum = pocketIncomes.sumOf { it.amount }
            val incomingTransfers = transfers.filter { it.toPocketId == pocket.id }.sumOf { it.amount }
            val outgoingTransfers = transfers.filter { it.fromPocketId == pocket.id }.sumOf { it.amount }
            val balance = incomeSum + incomingTransfers - outgoingTransfers
            val todayAdded = pocketIncomes
                .filter { it.dateMillis in todayStart..todayEnd }
                .sumOf { it.amount }
            val remainingPocket = (pocket.targetAmount - balance).coerceAtLeast(0L)
            val pocketDays = pocketIncomes.map { startOfDayMillis(it.dateMillis) }.toHashSet().size.coerceAtLeast(1)
            val pocketAvg = if (incomeSum > 0L) incomeSum / pocketDays else effectiveDailyAverage
            val estDays = when {
                pocket.targetAmount <= 0L -> null
                remainingPocket == 0L -> 0
                pocketAvg > 0L -> kotlin.math.ceil(remainingPocket.toDouble() / pocketAvg.toDouble()).toInt()
                else -> null
            }
            PocketWithBalance(
                pocket = pocket,
                balance = balance,
                todayIncome = todayAdded,
                entryCount = pocketIncomes.size,
                remainingToTarget = remainingPocket,
                estimatedDaysRemaining = estDays
            )
        }

        // 7-day bar chart window shifted by weekOffset (0 = last 7 days ending today)
        val windowEndDay = todayStart + (ctrl.weekOffset * 7L * DAY_MS)
        val windowStartDay = windowEndDay - 6L * DAY_MS
        val daySums = incomes.groupBy { startOfDayMillis(it.dateMillis) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val dailyBarPoints = (0..6).map { offset ->
            val dayMs = windowStartDay + offset * DAY_MS
            DailyBarPoint(
                dateMillis = dayMs,
                dayShort = formatDayShort(dayMs),
                dateLabel = formatDayMonth(dayMs),
                totalAmount = daySums[dayMs] ?: 0L,
                isToday = dayMs == todayStart
            )
        }
        val barRangeLabel = "${formatDayMonth(windowStartDay)} – ${formatDayMonth(windowEndDay)}"

        // Trend line chart points (Weekly: 8 weeks, Monthly: 6 months)
        val trendLinePoints = when (ctrl.trendPeriod) {
            TrendPeriod.WEEKLY -> buildWeeklyTrendPoints(incomes, todayStart)
            TrendPeriod.MONTHLY -> buildMonthlyTrendPoints(incomes, now)
        }

        // Category summaries across all recorded incomes
        val totalAll = totalBalance.coerceAtLeast(1L)
        val categorySummaries = categories.map { cat ->
            val catIncomes = incomes.filter { it.categoryId == cat.id }
            val sum = catIncomes.sumOf { it.amount }
            CategorySummary(
                category = cat,
                totalAmount = sum,
                percentage = if (totalBalance == 0L) 0f else (sum.toFloat() / totalAll.toFloat()),
                count = catIncomes.size
            )
        }.sortedByDescending { it.totalAmount }

        // Filtered history
        val query = ctrl.searchQuery.trim().lowercase()
        val filteredHistory = detailedIncomes.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.income.title.lowercase().contains(query) ||
                item.income.note.lowercase().contains(query) ||
                (item.category?.name?.lowercase()?.contains(query) == true) ||
                (item.pocket?.name?.lowercase()?.contains(query) == true)

            val matchesPeriod = when (ctrl.filterPeriod) {
                HistoryFilterPeriod.ALL -> true
                HistoryFilterPeriod.TODAY -> item.income.dateMillis in todayStart..todayEnd
                HistoryFilterPeriod.WEEK -> item.income.dateMillis in weekStart..todayEnd
                HistoryFilterPeriod.MONTH -> item.income.dateMillis in monthStart..todayEnd
            }

            val matchesCategory = ctrl.filterCategoryId == null || item.income.categoryId == ctrl.filterCategoryId
            val matchesPocket = ctrl.filterPocketId == null || item.income.pocketId == ctrl.filterPocketId

            matchesQuery && matchesPeriod && matchesCategory && matchesPocket
        }

        return KasUiState(
            isReady = categories.isNotEmpty() && pockets.isNotEmpty(),
            totalBalance = totalBalance,
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            monthTotal = monthTotal,
            dailyAverageThisMonth = dailyAvgMonth,
            activeDaysStreak = streak,
            dailyTarget = dailyTarget,
            savingsGoal = savingsGoal,
            categories = categories,
            pockets = pocketsWithBalance,
            allIncomes = detailedIncomes,
            todayIncomes = todayIncomes,
            filteredHistoryIncomes = filteredHistory,
            recentTransfers = transfers.take(15),
            weekOffset = ctrl.weekOffset,
            barChartRangeLabel = barRangeLabel,
            dailyBarPoints = dailyBarPoints,
            trendPeriod = ctrl.trendPeriod,
            trendLinePoints = trendLinePoints,
            categorySummaries = categorySummaries,
            searchQuery = ctrl.searchQuery,
            selectedFilterPeriod = ctrl.filterPeriod,
            selectedFilterCategoryId = ctrl.filterCategoryId,
            selectedFilterPocketId = ctrl.filterPocketId
        )
    }

    private fun buildWeeklyTrendPoints(incomes: List<IncomeEntity>, todayStart: Long): List<TrendLinePoint> {
        // 8 rolling 7-day weeks ending today
        return (7 downTo 0).map { weeksAgo ->
            val endDay = endOfDayMillis(todayStart - weeksAgo * 7L * DAY_MS)
            val startDay = startOfDayMillis(endDay - 6L * DAY_MS)
            val matching = incomes.filter { it.dateMillis in startDay..endDay }
            val label = if (weeksAgo == 0) "Mgg Ini" else formatDayMonth(startDay)
            val detail = "${formatDayMonth(startDay)} – ${formatDayMonth(endDay)}"
            TrendLinePoint(
                label = label,
                detailLabel = detail,
                totalAmount = matching.sumOf { it.amount },
                entryCount = matching.size
            )
        }
    }

    private fun buildMonthlyTrendPoints(incomes: List<IncomeEntity>, nowMillis: Long): List<TrendLinePoint> {
        return (5 downTo 0).map { monthsAgo ->
            val calStart = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, -monthsAgo)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = calStart.timeInMillis
            val calEnd = (calStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endMs = calEnd.timeInMillis
            val matching = incomes.filter { it.dateMillis in startMs..endMs }
            TrendLinePoint(
                label = formatMonthShort(startMs),
                detailLabel = formatMonthYear(startMs),
                totalAmount = matching.sumOf { it.amount },
                entryCount = matching.size
            )
        }
    }

    fun shiftBarWeek(delta: Int) {
        val next = (controlState.value.weekOffset + delta).coerceAtMost(0)
        controlState.value = controlState.value.copy(weekOffset = next)
    }

    fun setTrendPeriod(period: TrendPeriod) {
        controlState.value = controlState.value.copy(trendPeriod = period)
    }

    fun setSearchQuery(query: String) {
        controlState.value = controlState.value.copy(searchQuery = query)
    }

    fun setFilterPeriod(period: HistoryFilterPeriod) {
        controlState.value = controlState.value.copy(filterPeriod = period)
    }

    fun setFilterCategory(categoryId: Long?) {
        controlState.value = controlState.value.copy(filterCategoryId = categoryId)
    }

    fun setFilterPocket(pocketId: Long?) {
        controlState.value = controlState.value.copy(filterPocketId = pocketId)
    }

    fun clearHistoryFilters() {
        controlState.value = controlState.value.copy(
            searchQuery = "",
            filterPeriod = HistoryFilterPeriod.ALL,
            filterCategoryId = null,
            filterPocketId = null
        )
    }

    fun addIncome(
        title: String,
        amount: Long,
        categoryId: Long,
        pocketId: Long,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addIncome(title, amount, categoryId, pocketId, dateMillis, note)
            _snackbarMessages.tryEmit("Pemasukan \"$title\" berhasil disimpan")
        }
    }

    fun updateIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.updateIncome(income)
            _snackbarMessages.tryEmit("Pemasukan diperbarui")
        }
    }

    fun deleteIncome(incomeId: Long) {
        viewModelScope.launch {
            repository.deleteIncome(incomeId)
            _snackbarMessages.tryEmit("Pemasukan dihapus")
        }
    }

    fun createCategory(name: String, iconKey: String, colorHex: Long, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createCategory(name, iconKey, colorHex)
            _snackbarMessages.tryEmit("Kategori \"$name\" ditambahkan")
            onCreated(id)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _snackbarMessages.tryEmit("Kategori \"${category.name}\" diperbarui")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val all = uiState.value.categories
            val fallback = all.firstOrNull { it.id != category.id && it.name.equals("Lainnya", ignoreCase = true) }
                ?: all.firstOrNull { it.id != category.id }
            if (fallback == null) {
                _snackbarMessages.tryEmit("Minimal harus ada 1 kategori aktif")
                return@launch
            }
            repository.deleteCategory(category.id, fallback.id)
            if (controlState.value.filterCategoryId == category.id) {
                controlState.value = controlState.value.copy(filterCategoryId = null)
            }
            _snackbarMessages.tryEmit("Kategori \"${category.name}\" dihapus")
        }
    }

    fun createPocket(name: String, iconKey: String, colorHex: Long, targetAmount: Long) {
        viewModelScope.launch {
            repository.createPocket(name, iconKey, colorHex, targetAmount)
            _snackbarMessages.tryEmit("Kantong \"$name\" ditambahkan")
        }
    }

    fun updatePocket(pocket: PocketEntity) {
        viewModelScope.launch {
            repository.updatePocket(pocket)
            _snackbarMessages.tryEmit("Kantong \"${pocket.name}\" diperbarui")
        }
    }

    fun deletePocket(pocket: PocketEntity) {
        viewModelScope.launch {
            val all = uiState.value.pockets.map { it.pocket }
            val fallback = all.firstOrNull { it.id != pocket.id }
            if (fallback == null) {
                _snackbarMessages.tryEmit("Minimal harus ada 1 kantong penyimpanan")
                return@launch
            }
            repository.deletePocket(pocket.id, fallback.id)
            if (controlState.value.filterPocketId == pocket.id) {
                controlState.value = controlState.value.copy(filterPocketId = null)
            }
            _snackbarMessages.tryEmit("Kantong \"${pocket.name}\" dihapus")
        }
    }

    fun transferBetweenPockets(fromPocketId: Long, toPocketId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            repository.transferBetweenPockets(fromPocketId, toPocketId, amount, note)
            _snackbarMessages.tryEmit("Pemindahan saldo berhasil dicatat")
        }
    }

    fun updateDailyTarget(target: Long) {
        viewModelScope.launch {
            repository.updateDailyTarget(target)
            _snackbarMessages.tryEmit("Target harian diperbarui")
        }
    }

    fun updateSavingsGoal(goalName: String, targetAmount: Long) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goalName, targetAmount)
            _snackbarMessages.tryEmit("Target tabungan \"$goalName\" diperbarui")
        }
    }

    companion object {
        private const val DAY_MS = 86_400_000L

        fun factory(repository: KasHarianRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KasHarianViewModel(repository) as T
                }
            }
    }
}
