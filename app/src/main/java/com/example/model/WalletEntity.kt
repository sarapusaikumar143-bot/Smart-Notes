package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WalletType {
    CASH, BANK, UPI, CARD, SAVINGS
}

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val balance: Double,
    val colorHex: String,
    val iconName: String,
    val isDefault: Boolean = false
)
