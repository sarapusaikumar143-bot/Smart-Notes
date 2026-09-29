package com.example.data

import com.example.model.NoteEntity
import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.model.WalletEntity
import com.example.model.WalletType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AppRepository(
    private val transactionDao: TransactionDao,
    private val walletDao: WalletDao,
    private val noteDao: NoteDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allWallets: Flow<List<WalletEntity>> = walletDao.getAllWallets()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startTime, endTime)
    }

    suspend fun getTransactionsBetweenSync(startTime: Long, endTime: Long): List<TransactionEntity> {
        return transactionDao.getTransactionsBetweenSync(startTime, endTime)
    }

    fun getTransactionsForWallet(walletId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForWallet(walletId)
    }

    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        return if (query.isBlank()) noteDao.getAllNotes() else noteDao.searchNotes(query)
    }

    suspend fun ensureDefaultWalletsAndSeedData() {
        val count = walletDao.getWalletCount()
        if (count == 0) {
            val defaultWallets = listOf(
                WalletEntity(
                    id = 1,
                    name = "Bank Account",
                    type = WalletType.BANK,
                    balance = 34500.0,
                    colorHex = "#3B82F6",
                    iconName = "account_balance",
                    isDefault = true
                ),
                WalletEntity(
                    id = 2,
                    name = "UPI / Digital",
                    type = WalletType.UPI,
                    balance = 4850.0,
                    colorHex = "#22C55E",
                    iconName = "qr_code",
                    isDefault = false
                ),
                WalletEntity(
                    id = 3,
                    name = "Cash Wallet",
                    type = WalletType.CASH,
                    balance = 2200.0,
                    colorHex = "#F59E0B",
                    iconName = "payments",
                    isDefault = false
                ),
                WalletEntity(
                    id = 4,
                    name = "Credit Card",
                    type = WalletType.CARD,
                    balance = 15000.0,
                    colorHex = "#8B5CF6",
                    iconName = "credit_card",
                    isDefault = false
                )
            )
            walletDao.insertAllWallets(defaultWallets)

            // Seed initial realistic transactions for this month so calendar & analytics immediately look stunning!
            val cal = Calendar.getInstance()
            val nowMillis = cal.timeInMillis

            // Today
            val todayDay = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            val seedTransactions = mutableListOf<TransactionEntity>()

            fun getCalMillis(day: Int, hour: Int = 12): Long {
                val c = Calendar.getInstance()
                c.set(year, month, day.coerceIn(1, 28), hour, 0, 0)
                return c.timeInMillis
            }

            // Month start salary
            seedTransactions.add(
                TransactionEntity(
                    title = "Monthly Salary",
                    amount = 55000.0,
                    type = TransactionType.INCOME,
                    category = "Salary",
                    walletId = 1,
                    dateMillis = getCalMillis(1, 10),
                    note = "Direct bank deposit"
                )
            )
            // Groceries
            seedTransactions.add(
                TransactionEntity(
                    title = "Supermarket & Groceries",
                    amount = 2450.0,
                    type = TransactionType.EXPENSE,
                    category = "Food",
                    walletId = 2,
                    dateMillis = getCalMillis(2, 17),
                    note = "Weekly vegetables & essentials"
                )
            )
            // Fuel
            seedTransactions.add(
                TransactionEntity(
                    title = "Petrol Refill",
                    amount = 650.0,
                    type = TransactionType.EXPENSE,
                    category = "Transport",
                    walletId = 2,
                    dateMillis = getCalMillis(3, 9),
                    note = "Bike tank full"
                )
            )
            // Dining / Biryani
            seedTransactions.add(
                TransactionEntity(
                    title = "Biryani & Starters",
                    amount = 580.0,
                    type = TransactionType.EXPENSE,
                    category = "Food",
                    walletId = 2,
                    dateMillis = getCalMillis(todayDay.coerceAtLeast(2) - 1, 20),
                    note = "Dinner with friends"
                )
            )
            // Electricity Bill
            seedTransactions.add(
                TransactionEntity(
                    title = "Electricity & Utility Bill",
                    amount = 1850.0,
                    type = TransactionType.EXPENSE,
                    category = "Bills",
                    walletId = 1,
                    dateMillis = getCalMillis(5, 14),
                    note = "Paid via NetBanking"
                )
            )
            // Freelance / Bonus
            seedTransactions.add(
                TransactionEntity(
                    title = "Freelance Project Milestone",
                    amount = 12000.0,
                    type = TransactionType.INCOME,
                    category = "Investment",
                    walletId = 1,
                    dateMillis = getCalMillis(8, 11),
                    note = "Client payment received"
                )
            )
            // Coffee / Snack today
            seedTransactions.add(
                TransactionEntity(
                    title = "Espresso & Croissant",
                    amount = 220.0,
                    type = TransactionType.EXPENSE,
                    category = "Food",
                    walletId = 3,
                    dateMillis = nowMillis,
                    note = "Morning coffee"
                )
            )

            seedTransactions.forEach { transactionDao.insertTransaction(it) }

            // Seed initial helpful notes
            if (noteDao.getNotesCount() == 0) {
                noteDao.insertNote(
                    NoteEntity(
                        title = "Monthly Savings Goals & Rules",
                        content = "1. Limit weekend dining out to ₹1,500 max.\n2. Keep UPI wallet topped up only with weekly budget.\n3. Transfer 20% to emergency savings every 1st of month.\n4. Check AI prediction daily before any impulse purchase.",
                        isChecklist = false,
                        category = "Finance",
                        colorHex = "#22C55E"
                    )
                )
                noteDao.insertNote(
                    NoteEntity(
                        title = "Weekend Shopping List",
                        content = "Items to restock for home",
                        isChecklist = true,
                        checklistJson = """[{"text":"Basmati Rice 5kg","done":true},{"text":"Olive oil & Spices","done":false},{"text":"Coffee beans","done":true},{"text":"Almonds & Walnuts","done":false}]""",
                        category = "Groceries",
                        colorHex = "#3B82F6"
                    )
                )
                noteDao.insertNote(
                    NoteEntity(
                        title = "Laptop EMI & Maintenance Note",
                        content = "Hardware service done on 15th. Total expense ₹1,400 paid via card. Claim warranty before next quarter.",
                        isChecklist = false,
                        category = "Tech",
                        colorHex = "#8B5CF6"
                    )
                )
            }
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
                if (transaction.toWalletId != null) {
                    walletDao.updateWalletBalance(transaction.toWalletId, transaction.amount)
                }
            }
        }
        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // Reverse balance
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
            }
            TransactionType.INCOME -> {
                walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
                if (transaction.toWalletId != null) {
                    walletDao.updateWalletBalance(transaction.toWalletId, -transaction.amount)
                }
            }
        }
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun transferFunds(
        fromWalletId: Long,
        toWalletId: Long,
        amount: Double,
        title: String = "Wallet Transfer",
        note: String = ""
    ): Long {
        val transaction = TransactionEntity(
            title = title,
            amount = amount,
            type = TransactionType.TRANSFER,
            category = "Transfer",
            walletId = fromWalletId,
            toWalletId = toWalletId,
            dateMillis = System.currentTimeMillis(),
            note = note
        )
        return addTransaction(transaction)
    }

    // Wallet Operations
    suspend fun addWallet(wallet: WalletEntity): Long = walletDao.insertWallet(wallet)
    suspend fun updateWallet(wallet: WalletEntity) = walletDao.updateWallet(wallet)
    suspend fun deleteWallet(wallet: WalletEntity) = walletDao.deleteWallet(wallet)
    suspend fun getWalletById(id: Long): WalletEntity? = walletDao.getWalletById(id)

    // Note Operations
    suspend fun addNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
}
