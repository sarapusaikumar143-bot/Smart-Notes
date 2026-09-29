package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DailyInsight
import com.example.ai.FinancialChatAssistant
import com.example.ai.FinancialPrediction
import com.example.ai.GeminiAiService
import com.example.ai.NoteAiAssistant
import com.example.ai.ParsedExpense
import com.example.ai.PredictionEngine
import com.example.ai.SmartExpenseParser
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.model.NoteEntity
import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.model.WalletEntity
import com.example.model.WalletType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab {
    CALENDAR, ANALYTICS, AI_ASSISTANT, NOTES, WALLETS
}

data class ChatMessageItem(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.transactionDao(), db.walletDao(), db.noteDao())

    val geminiService = GeminiAiService()
    val expenseParser = SmartExpenseParser(geminiService)
    val predictionEngine = PredictionEngine()
    val chatAssistant = FinancialChatAssistant(geminiService, predictionEngine)
    val noteAiAssistant = NoteAiAssistant(geminiService, expenseParser)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.CALENDAR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Calendar Selected Date (millis)
    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    // Calendar Current Month & Year
    private val _currentMonthYear = MutableStateFlow(Pair(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)))
    val currentMonthYear: StateFlow<Pair<Int, Int>> = _currentMonthYear.asStateFlow()

    // Transaction Filter (All, Expense, Income)
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    private val _selectedWalletFilter = MutableStateFlow<Long?>(null)
    val selectedWalletFilter: StateFlow<Long?> = _selectedWalletFilter.asStateFlow()

    // Room DB Flows
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWallets: StateFlow<List<WalletEntity>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Assistant messages
    private val _chatMessages = MutableStateFlow(listOf(
        ChatMessageItem(
            id = "1",
            text = "👋 Hello! I'm your AI Money & Notes Assistant. Ask me how to save more, analyze your burn rate, predict runway, or link expenses from your notes!",
            isUser = false
        )
    ))
    val chatMessages: StateFlow<List<ChatMessageItem>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // App Lock State
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _userPin = MutableStateFlow("1234") // default demo PIN
    val userPin: StateFlow<String> = _userPin.asStateFlow()

    private val _isPinProtectionEnabled = MutableStateFlow(false)
    val isPinProtectionEnabled: StateFlow<Boolean> = _isPinProtectionEnabled.asStateFlow()

    // Quick Add Sheet State
    private val _showAddTransactionSheet = MutableStateFlow(false)
    val showAddTransactionSheet: StateFlow<Boolean> = _showAddTransactionSheet.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultWalletsAndSeedData()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        _currentMonthYear.value = Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }

    fun changeMonth(delta: Int) {
        val (month, year) = _currentMonthYear.value
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, delta)
        _currentMonthYear.value = Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
        _selectedDateMillis.value = cal.timeInMillis
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setSelectedWalletFilter(walletId: Long?) {
        _selectedWalletFilter.value = walletId
    }

    fun openAddTransactionSheet() {
        _showAddTransactionSheet.value = true
    }

    fun closeAddTransactionSheet() {
        _showAddTransactionSheet.value = false
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        walletId: Long,
        toWalletId: Long? = null,
        dateMillis: Long = _selectedDateMillis.value,
        note: String = ""
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                walletId = walletId,
                toWalletId = toWalletId,
                dateMillis = dateMillis,
                note = note
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun transferFunds(fromWalletId: Long, toWalletId: Long, amount: Double, note: String = "") {
        viewModelScope.launch {
            repository.transferFunds(fromWalletId, toWalletId, amount, note = note)
        }
    }

    fun addWallet(name: String, type: WalletType, balance: Double, colorHex: String) {
        viewModelScope.launch {
            val iconName = when (type) {
                WalletType.CASH -> "payments"
                WalletType.BANK -> "account_balance"
                WalletType.UPI -> "qr_code"
                WalletType.CARD -> "credit_card"
                WalletType.SAVINGS -> "savings"
            }
            repository.addWallet(
                WalletEntity(
                    name = name,
                    type = type,
                    balance = balance,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.addNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun sendChatMessage(query: String) {
        val userMsg = ChatMessageItem(
            id = System.currentTimeMillis().toString(),
            text = query,
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val response = chatAssistant.answerFinancialQuery(
                query = query,
                wallets = allWallets.value,
                transactions = allTransactions.value
            )
            val aiMsg = ChatMessageItem(
                id = (System.currentTimeMillis() + 1).toString(),
                text = response,
                isUser = false
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isChatLoading.value = false
        }
    }

    fun toggleAppLock(enabled: Boolean, pin: String = "1234") {
        _isPinProtectionEnabled.value = enabled
        _userPin.value = pin
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    suspend fun summarizeNoteContent(content: String): String {
        return noteAiAssistant.summarizeNote(content)
    }

    suspend fun convertNoteToChecklistItems(content: String): List<Pair<String, Boolean>> {
        return noteAiAssistant.convertToChecklist(content)
    }

    suspend fun translateNoteContent(content: String, targetLanguage: String): String {
        return noteAiAssistant.translateNote(content, targetLanguage)
    }

    suspend fun detectExpenseFromNoteContent(content: String): ParsedExpense? {
        return noteAiAssistant.detectExpenseInNote(content)
    }

    fun toggleChecklistItemInNote(noteId: Long, itemId: String) {
        viewModelScope.launch {
            val note = allNotes.value.find { it.id == noteId } ?: return@launch
            try {
                val array = org.json.JSONArray(note.checklistJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.optString("id") == itemId || obj.optString("text") == itemId) {
                        val currentDone = obj.optBoolean("done", false)
                        obj.put("done", !currentDone)
                        break
                    }
                }
                repository.updateNote(note.copy(checklistJson = array.toString(), updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun unlockApp(enteredPin: String): Boolean {
        if (enteredPin == _userPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_isPinProtectionEnabled.value) {
            _isAppLocked.value = true
        }
    }
}
