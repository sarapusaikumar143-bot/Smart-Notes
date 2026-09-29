package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.models.ChecklistItem
import com.example.domain.models.Note
import com.example.domain.models.Transaction
import com.example.model.NoteEntity
import com.example.model.TransactionType
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.QuickAddTransactionSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TransferDialog
import com.example.ui.components.VoiceInputDialog
import com.example.ui.screens.AIScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FilesExplorerScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.DarkNavy
import com.example.viewmodel.MainViewModel
import org.json.JSONArray
import java.util.Calendar

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val wallets by viewModel.allWallets.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()

    val currentMonthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val isPinProtectionEnabled by viewModel.isPinProtectionEnabled.collectAsStateWithLifecycle()

    var showVoiceDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var quickAddDateMillis by remember { mutableStateOf(selectedDateMillis) }

    val dailyInsight = remember(transactions, selectedDateMillis) {
        viewModel.predictionEngine.generateDailyInsight(transactions, selectedDateMillis)
    }
    val prediction = remember(transactions, wallets) {
        viewModel.predictionEngine.computePrediction(transactions, wallets)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showFab = currentRoute == Screen.Calendar.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            if (currentRoute != Screen.AddTransaction.route) {
                BottomNavBar(navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                AddTransactionFAB(navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Calendar.route
            ) {
                // 1. CalendarScreen (Home)
                composable(Screen.Calendar.route) {
                    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    val selDay = selCal.get(Calendar.DAY_OF_MONTH)

                    CalendarScreen(
                        currentMonth = currentMonthYear.first,
                        currentYear = currentMonthYear.second,
                        selectedDay = selDay,
                        transactions = transactions.map { it.toDomainModel() },
                        dailyInsight = dailyInsight.toDomainInsight(),
                        onDaySelected = { day ->
                            val c = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentMonthYear.second)
                                set(Calendar.MONTH, currentMonthYear.first)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            viewModel.setSelectedDate(c.timeInMillis)
                        },
                        onChangeMonth = { delta ->
                            viewModel.changeMonth(delta)
                        },
                        onOpenSettings = {
                            showSettingsDialog = true
                        },
                        onOpenFiles = {
                            navController.navigate(Screen.Files.route)
                        },
                        onQuickAdd = { day ->
                            val c = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentMonthYear.second)
                                set(Calendar.MONTH, currentMonthYear.first)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            quickAddDateMillis = c.timeInMillis
                            showQuickAddSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            val entity = transactions.find { it.id == tx.id }
                            if (entity != null) {
                                viewModel.deleteTransaction(entity)
                            }
                        }
                    )
                }

                // 2. AnalyticsScreen
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(
                        transactions = transactions.map { it.toDomainModel() },
                        prediction = prediction.toDomainPrediction()
                    )
                }

                // 3. AIScreen
                composable(Screen.AI.route) {
                    AIScreen(
                        messages = chatMessages,
                        isLoading = isChatLoading,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onVoiceClick = { showVoiceDialog = true }
                    )
                }

                // 4. NotesScreen
                composable(Screen.Notes.route) {
                    NotesScreen(
                        notes = notes.map { it.toDomainNote() },
                        onSaveNote = { noteEntity ->
                            viewModel.saveNote(noteEntity)
                        },
                        onDeleteNote = { note ->
                            viewModel.deleteNote(
                                NoteEntity(id = note.id, title = note.title, content = note.content)
                            )
                        },
                        onToggleChecklistItem = { note, itemId ->
                            viewModel.toggleChecklistItemInNote(note.id, itemId)
                        },
                        onSummarize = { content ->
                            viewModel.summarizeNoteContent(content)
                        },
                        onConvertToChecklist = { content ->
                            viewModel.convertNoteToChecklistItems(content)
                        },
                        onTranslate = { content, lang ->
                            viewModel.translateNoteContent(content, lang)
                        },
                        onDetectExpense = { content ->
                            viewModel.detectExpenseFromNoteContent(content)
                        },
                        onCreateExpenseFromNote = { title, amount, type, category ->
                            viewModel.addTransaction(
                                title = title,
                                amount = amount,
                                type = type,
                                category = category,
                                walletId = wallets.firstOrNull()?.id ?: 1L,
                                dateMillis = System.currentTimeMillis()
                            )
                        }
                    )
                }

                // 5. WalletScreen
                composable(Screen.Wallet.route) {
                    WalletsScreen(
                        wallets = wallets,
                        onOpenTransferDialog = { showTransferDialog = true },
                        onAddWallet = { name, type, balance, colorHex ->
                            viewModel.addWallet(name, type, balance, colorHex)
                        }
                    )
                }

                // 6. AddTransactionScreen
                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(
                        wallets = wallets.map { it.toDomainWallet() },
                        onNavigateBack = { navController.popBackStack() },
                        onSaveTransaction = { tx ->
                            viewModel.addTransaction(
                                title = tx.title,
                                amount = tx.amount,
                                type = TransactionType.valueOf(tx.type.name),
                                category = tx.category.displayName,
                                walletId = tx.walletId,
                                dateMillis = tx.dateMillis,
                                note = tx.note
                            )
                        }
                    )
                }

                // 7. FilesExplorerScreen
                composable(Screen.Files.route) {
                    FilesExplorerScreen(
                        transactions = transactions,
                        wallets = wallets,
                        notes = notes
                    )
                }
            }
        }
    }

    if (showQuickAddSheet) {
        QuickAddTransactionSheet(
            targetDateMillis = quickAddDateMillis,
            wallets = wallets,
            expenseParser = viewModel.expenseParser,
            onDismiss = { showQuickAddSheet = false },
            onSaveTransaction = { title, amount, type, category, walletId, note ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    walletId = walletId,
                    dateMillis = quickAddDateMillis,
                    note = note
                )
                showQuickAddSheet = false
            },
            onOpenVoiceInput = {
                showQuickAddSheet = false
                showVoiceDialog = true
            }
        )
    }

    if (showVoiceDialog) {
        VoiceInputDialog(
            expenseParser = viewModel.expenseParser,
            onDismiss = { showVoiceDialog = false },
            onVoiceEntryConfirmed = { title, amount, type, category ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    walletId = wallets.firstOrNull()?.id ?: 1L,
                    dateMillis = selectedDateMillis
                )
            }
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            wallets = wallets,
            onDismiss = { showTransferDialog = false },
            onTransferConfirmed = { fromId, toId, amount, note ->
                viewModel.transferFunds(fromId, toId, amount, note)
            }
        )
    }

    if (showExportDialog) {
        ExportReportDialog(
            transactions = transactions,
            wallets = wallets,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isPinProtectionEnabled = isPinProtectionEnabled,
            onTogglePinProtection = { viewModel.toggleAppLock(it) },
            onOpenExport = { showExportDialog = true },
            onOpenFiles = { navController.navigate(Screen.Files.route) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun AddTransactionFAB(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate(Screen.AddTransaction.route) },
        containerColor = DarkNavy,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("add_transaction_fab")
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
    }
}

// Helpers to map entities to domain models for UI presentation
private fun com.example.model.TransactionEntity.toDomainModel() = com.example.domain.models.Transaction(
    id = id,
    title = title,
    amount = amount,
    type = com.example.domain.models.TransactionType.valueOf(type.name),
    category = com.example.domain.models.Category.fromString(category),
    walletId = walletId,
    toWalletId = toWalletId,
    dateMillis = dateMillis,
    note = note,
    createdAt = createdAt
)

private fun com.example.model.WalletEntity.toDomainWallet() = com.example.domain.models.Wallet(
    id = id,
    name = name,
    type = com.example.domain.models.WalletType.valueOf(type.name),
    balance = balance,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault
)

private fun com.example.model.NoteEntity.toDomainNote(): Note {
    val items = mutableListOf<ChecklistItem>()
    if (isChecklist && checklistJson.isNotBlank()) {
        try {
            val array = JSONArray(checklistJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                items.add(
                    ChecklistItem(
                        id = obj.optString("id", i.toString()),
                        text = obj.optString("text", ""),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
    }
    return Note(
        id = id,
        title = title,
        content = content,
        isChecklist = isChecklist,
        checklistItems = items,
        category = category,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun com.example.ai.DailyInsight.toDomainInsight() = com.example.domain.models.DailyInsight(
    title = title,
    description = description,
    badge = badge,
    changePercent = changePercent,
    isPositive = isPositive
)

private fun com.example.ai.FinancialPrediction.toDomainPrediction() = com.example.domain.models.FinancialPrediction(
    runwayDays = runwayDays,
    dailyBurnRate = dailyBurnRate,
    projectedMonthEndBalance = projectedMonthEndBalance,
    overspendingAlert = overspendingAlert,
    savingsRatePercent = savingsRatePercent,
    summaryText = summaryText
)
