package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val walletId: Long,
    val toWalletId: Long? = null,
    val dateMillis: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
