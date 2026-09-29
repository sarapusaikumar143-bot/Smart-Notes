package com.example.domain.usecases

import com.example.domain.models.Transaction
import com.example.domain.models.Wallet
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<Transaction>> = repository.getAllTransactions()
}

class AddTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Long = repository.addTransaction(transaction)
}

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.deleteTransaction(transaction)
}

class GetWalletsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<Wallet>> = repository.getAllWallets()
}

class TransferFundsUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(fromWalletId: Long, toWalletId: Long, amount: Double, note: String = ""): Long =
        repository.transferFunds(fromWalletId, toWalletId, amount, note)
}
