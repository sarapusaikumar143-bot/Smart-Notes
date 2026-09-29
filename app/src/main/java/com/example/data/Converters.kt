package com.example.data

import androidx.room.TypeConverter
import com.example.model.TransactionType
import com.example.model.WalletType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    @TypeConverter
    fun fromWalletType(value: WalletType): String = value.name

    @TypeConverter
    fun toWalletType(value: String): WalletType = try {
        WalletType.valueOf(value)
    } catch (e: Exception) {
        WalletType.CASH
    }
}
