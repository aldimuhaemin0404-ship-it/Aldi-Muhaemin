package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.IncomeEntity
import com.example.ui.components.AddEditIncomeSheet
import com.example.ui.components.CategoryManagerSheet
import com.example.ui.components.DailyTargetDialog
import com.example.ui.components.SavingsGoalDialog
import com.example.ui.screens.AnalitikScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.KantongScreen
import com.example.ui.screens.RiwayatScreen

const val ROUTE_BERANDA = "beranda"
const val ROUTE_ANALITIK = "analitik"
const val ROUTE_KANTONG = "kantong"
const val ROUTE_RIWAYAT = "riwayat"

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navDestinations = listOf(
    NavDestination(ROUTE_BERANDA, "Beranda", Icons.Rounded.Home, Icons.Outlined.Home),
    NavDestination(ROUTE_ANALITIK, "Analitik", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    NavDestination(ROUTE_KANTONG, "Kantong", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    NavDestination(ROUTE_RIWAYAT, "Riwayat", Icons.Rounded.History, Icons.Outlined.History)
)

@Composable
fun KasHarianApp(viewModel: KasHarianViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_BERANDA

    val snackbarHostState = remember { SnackbarHostState() }

    var showIncomeSheet by remember { mutableStateOf(false) }
    var editingIncome by remember { mutableStateOf<IncomeEntity?>(null) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var showDailyTargetDialog by remember { mutableStateOf(false) }
    var showSavingsGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentRoute != ROUTE_KANTONG) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingIncome = null
                            showIncomeSheet = true
                        },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = { Text("Catat") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_income")
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            FloatingActionButton(
                                onClick = {
                                    editingIncome = null
                                    showIncomeSheet = true
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Catat Pemasukan")
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_BERANDA,
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxSize()
                    ) {
                        composable(ROUTE_BERANDA) {
                            BerandaScreen(
                                state = state,
                                onAddIncomeClick = {
                                    editingIncome = null
                                    showIncomeSheet = true
                                },
                                onEditIncomeClick = { income ->
                                    editingIncome = income
                                    showIncomeSheet = true
                                },
                                onEditDailyTargetClick = { showDailyTargetDialog = true },
                                onEditSavingsGoalClick = { showSavingsGoalDialog = true },
                                onManageCategoriesClick = { showCategoryManager = true },
                                onShiftBarWeek = viewModel::shiftBarWeek,
                                onNavigateToAnalytics = { navigateToTab(ROUTE_ANALITIK) },
                                onNavigateToPockets = { navigateToTab(ROUTE_KANTONG) }
                            )
                        }
                        composable(ROUTE_ANALITIK) {
                            AnalitikScreen(
                                state = state,
                                onShiftBarWeek = viewModel::shiftBarWeek,
                                onSelectTrendPeriod = viewModel::setTrendPeriod,
                                onManageCategoriesClick = { showCategoryManager = true }
                            )
                        }
                        composable(ROUTE_KANTONG) {
                            KantongScreen(
                                state = state,
                                onEditSavingsGoalClick = { showSavingsGoalDialog = true },
                                onCreatePocket = viewModel::createPocket,
                                onUpdatePocket = viewModel::updatePocket,
                                onDeletePocket = viewModel::deletePocket,
                                onTransfer = viewModel::transferBetweenPockets
                            )
                        }
                        composable(ROUTE_RIWAYAT) {
                            RiwayatScreen(
                                state = state,
                                onSearchQueryChange = viewModel::setSearchQuery,
                                onSelectPeriod = viewModel::setFilterPeriod,
                                onSelectCategory = viewModel::setFilterCategory,
                                onSelectPocket = viewModel::setFilterPocket,
                                onClearFilters = viewModel::clearHistoryFilters,
                                onEditIncomeClick = { income ->
                                    editingIncome = income
                                    showIncomeSheet = true
                                },
                                onManageCategoriesClick = { showCategoryManager = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIncomeSheet && state.isReady) {
        AddEditIncomeSheet(
            initialIncome = editingIncome,
            categories = state.categories,
            pockets = state.pockets.map { it.pocket },
            onDismiss = {
                showIncomeSheet = false
                editingIncome = null
            },
            onSave = { title, amount, categoryId, pocketId, dateMillis, note ->
                val existing = editingIncome
                if (existing == null) {
                    viewModel.addIncome(title, amount, categoryId, pocketId, dateMillis, note)
                } else {
                    viewModel.updateIncome(
                        existing.copy(
                            title = title,
                            amount = amount,
                            categoryId = categoryId,
                            pocketId = pocketId,
                            dateMillis = dateMillis,
                            note = note
                        )
                    )
                }
                showIncomeSheet = false
                editingIncome = null
            },
            onDelete = { incomeId ->
                viewModel.deleteIncome(incomeId)
                showIncomeSheet = false
                editingIncome = null
            },
            onQuickCreateCategory = { name, iconKey, colorHex, onCreated ->
                viewModel.createCategory(name, iconKey, colorHex, onCreated)
            }
        )
    }

    if (showCategoryManager) {
        CategoryManagerSheet(
            categories = state.categories,
            summaries = state.categorySummaries,
            onDismiss = { showCategoryManager = false },
            onCreateCategory = { name, iconKey, colorHex ->
                viewModel.createCategory(name, iconKey, colorHex)
            },
            onUpdateCategory = viewModel::updateCategory,
            onDeleteCategory = viewModel::deleteCategory
        )
    }

    if (showDailyTargetDialog) {
        DailyTargetDialog(
            currentTarget = state.dailyTarget,
            onDismiss = { showDailyTargetDialog = false },
            onSave = { newTarget ->
                viewModel.updateDailyTarget(newTarget)
                showDailyTargetDialog = false
            }
        )
    }

    if (showSavingsGoalDialog) {
        SavingsGoalDialog(
            currentName = state.savingsGoal.goalName,
            currentTarget = state.savingsGoal.targetAmount,
            onDismiss = { showSavingsGoalDialog = false },
            onSave = { name, target ->
                viewModel.updateSavingsGoal(name, target)
                showSavingsGoalDialog = false
            }
        )
    }
}
