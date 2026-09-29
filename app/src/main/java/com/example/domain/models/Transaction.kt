package com.example.domain.models

enum class Category(val displayName: String, val colorHex: String) {
    FOOD("Food", "#F59E0B"),
    TRANSPORT("Transport", "#3B82F6"),
    BILLS("Bills", "#8B5CF6"),
    SHOPPING("Shopping", "#EC4899"),
    ENTERTAINMENT("Entertainment", "#06B6D4"),
    HEALTH("Health", "#EF4444"),
    SALARY("Salary", "#22C55E"),
    INVESTMENT("Investment", "#10B981"),
    OTHER("Other", "#64748B");

    companion object {
        fun fromString(name: String): Category {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val walletId: Long,
    val toWalletId: Long? = null,
    val dateMillis: Long,
    val note: String = "",
    val receiptUri: String? = null,
    val repeatInterval: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
