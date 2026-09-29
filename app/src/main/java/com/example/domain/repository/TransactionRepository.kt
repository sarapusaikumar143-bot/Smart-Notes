package com.example.domain.repository

import com.example.domain.models.Note
import com.example.domain.models.Transaction
import com.example.domain.models.Wallet
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(transaction: Transaction)
    
    fun getAllWallets(): Flow<List<Wallet>>
    suspend fun addWallet(wallet: Wallet): Long
    suspend fun transferFunds(fromWalletId: Long, toWalletId: Long, amount: Double, note: String): Long
    
    fun getAllNotes(): Flow<List<Note>>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(note: Note)
    
    suspend fun ensureDefaultData()
}
