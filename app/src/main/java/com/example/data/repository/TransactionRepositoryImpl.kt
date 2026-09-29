package com.example.data.repository

import com.example.data.NoteDao
import com.example.data.TransactionDao
import com.example.data.WalletDao
import com.example.domain.models.Category
import com.example.domain.models.ChecklistItem
import com.example.domain.models.Note
import com.example.domain.models.Transaction
import com.example.domain.models.TransactionType
import com.example.domain.models.Wallet
import com.example.domain.models.WalletType
import com.example.domain.repository.TransactionRepository
import com.example.model.NoteEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val walletDao: WalletDao,
    private val noteDao: NoteDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetween(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Long {
        val entity = transaction.toEntity()
        val id = transactionDao.insertTransaction(entity)
        when (transaction.type) {
            TransactionType.EXPENSE -> walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
            TransactionType.INCOME -> walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
            TransactionType.TRANSFER -> {
                walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
                if (transaction.toWalletId != null) {
                    walletDao.updateWalletBalance(transaction.toWalletId, transaction.amount)
                }
            }
        }
        return id
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        when (transaction.type) {
            TransactionType.EXPENSE -> walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
            TransactionType.INCOME -> walletDao.updateWalletBalance(transaction.walletId, -transaction.amount)
            TransactionType.TRANSFER -> {
                walletDao.updateWalletBalance(transaction.walletId, transaction.amount)
                if (transaction.toWalletId != null) {
                    walletDao.updateWalletBalance(transaction.toWalletId, -transaction.amount)
                }
            }
        }
        transactionDao.deleteById(transaction.id)
    }

    override fun getAllWallets(): Flow<List<Wallet>> {
        return walletDao.getAllWallets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWallet(wallet: Wallet): Long {
        return walletDao.insertWallet(wallet.toEntity())
    }

    override suspend fun transferFunds(
        fromWalletId: Long,
        toWalletId: Long,
        amount: Double,
        note: String
    ): Long {
        val tx = Transaction(
            title = "Wallet Transfer",
            amount = amount,
            type = TransactionType.TRANSFER,
            category = Category.OTHER,
            walletId = fromWalletId,
            toWalletId = toWalletId,
            dateMillis = System.currentTimeMillis(),
            note = note
        )
        return addTransaction(tx)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNote(note: Note): Long {
        return if (note.id == 0L) {
            noteDao.insertNote(note.toEntity())
        } else {
            noteDao.updateNote(note.toEntity())
            note.id
        }
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNoteById(note.id)
    }

    override suspend fun ensureDefaultData() {
        if (walletDao.getWalletCount() == 0) {
            val defaultWallets = listOf(
                WalletEntity(id = 1, name = "Bank Account", type = com.example.model.WalletType.BANK, balance = 42200.0, colorHex = "#3B82F6", iconName = "account_balance", isDefault = true),
                WalletEntity(id = 2, name = "UPI / Digital", type = com.example.model.WalletType.UPI, balance = 5200.0, colorHex = "#22C55E", iconName = "qr_code", isDefault = false),
                WalletEntity(id = 3, name = "Cash Wallet", type = com.example.model.WalletType.CASH, balance = 2200.0, colorHex = "#F59E0B", iconName = "payments", isDefault = false),
                WalletEntity(id = 4, name = "Credit Card", type = com.example.model.WalletType.CARD, balance = 10000.0, colorHex = "#8B5CF6", iconName = "credit_card", isDefault = false)
            )
            walletDao.insertAllWallets(defaultWallets)

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val today = cal.get(Calendar.DAY_OF_MONTH)

            fun dateMillis(day: Int, hour: Int = 12): Long {
                val c = Calendar.getInstance()
                c.set(year, month, day.coerceIn(1, 28), hour, 0, 0)
                return c.timeInMillis
            }

            val seedTxs = listOf(
                TransactionEntity(title = "Monthly Salary Credit", amount = 55000.0, type = com.example.model.TransactionType.INCOME, category = "Salary", walletId = 1, dateMillis = dateMillis(1, 9), note = "Direct payroll deposit"),
                TransactionEntity(title = "Supermarket Groceries", amount = 2450.0, type = com.example.model.TransactionType.EXPENSE, category = "Food", walletId = 2, dateMillis = dateMillis(2, 17), note = "Monthly staples"),
                TransactionEntity(title = "Petrol Refill", amount = 650.0, type = com.example.model.TransactionType.EXPENSE, category = "Transport", walletId = 2, dateMillis = dateMillis(3, 8), note = "Fuel full tank"),
                TransactionEntity(title = "Biryani & Starters", amount = 580.0, type = com.example.model.TransactionType.EXPENSE, category = "Food", walletId = 2, dateMillis = dateMillis(today.coerceAtLeast(2) - 1, 20), note = "Dinner with friends"),
                TransactionEntity(title = "Electricity Bill", amount = 1850.0, type = com.example.model.TransactionType.EXPENSE, category = "Bills", walletId = 1, dateMillis = dateMillis(5, 14), note = "State board utility"),
                TransactionEntity(title = "Freelance Project Milestone", amount = 12000.0, type = com.example.model.TransactionType.INCOME, category = "Investment", walletId = 1, dateMillis = dateMillis(8, 11), note = "Client milestone payout"),
                TransactionEntity(title = "Coffee & Croissant", amount = 220.0, type = com.example.model.TransactionType.EXPENSE, category = "Food", walletId = 3, dateMillis = System.currentTimeMillis(), note = "Morning cafe")
            )
            seedTxs.forEach { transactionDao.insertTransaction(it) }

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
                        content = "Items to restock for kitchen",
                        isChecklist = true,
                        checklistJson = """[{"text":"Basmati Rice 5kg","done":true},{"text":"Olive oil & Spices","done":false},{"text":"Arabica Coffee beans","done":true},{"text":"Almonds & Walnuts","done":false}]""",
                        category = "Groceries",
                        colorHex = "#3B82F6"
                    )
                )
            }
        }
    }

    // Mappers
    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        title = title,
        amount = amount,
        type = TransactionType.valueOf(type.name),
        category = Category.fromString(category),
        walletId = walletId,
        toWalletId = toWalletId,
        dateMillis = dateMillis,
        note = note,
        createdAt = createdAt
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = com.example.model.TransactionType.valueOf(type.name),
        category = category.displayName,
        walletId = walletId,
        toWalletId = toWalletId,
        dateMillis = dateMillis,
        note = note,
        createdAt = createdAt
    )

    private fun WalletEntity.toDomain() = Wallet(
        id = id,
        name = name,
        type = WalletType.valueOf(type.name),
        balance = balance,
        colorHex = colorHex,
        iconName = iconName,
        isDefault = isDefault
    )

    private fun Wallet.toEntity() = WalletEntity(
        id = id,
        name = name,
        type = com.example.model.WalletType.valueOf(type.name),
        balance = balance,
        colorHex = colorHex,
        iconName = iconName,
        isDefault = isDefault
    )

    private fun NoteEntity.toDomain(): Note {
        val items = mutableListOf<ChecklistItem>()
        try {
            val arr = JSONArray(checklistJson)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                items.add(ChecklistItem(id = "item_$i", text = o.optString("text"), done = o.optBoolean("done")))
            }
        } catch (e: Exception) {
            // ignore
        }
        return Note(
            id = id,
            title = title,
            content = content,
            isChecklist = isChecklist,
            checklistItems = items,
            linkedTransactionId = linkedTransactionId,
            category = category,
            colorHex = colorHex,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        val arr = JSONArray()
        checklistItems.forEach {
            val o = JSONObject()
            o.put("text", it.text)
            o.put("done", it.done)
            arr.put(o)
        }
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            isChecklist = isChecklist,
            checklistJson = arr.toString(),
            linkedTransactionId = linkedTransactionId,
            category = category,
            colorHex = colorHex,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
