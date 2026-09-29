package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    suspend fun getAllWalletsSync(): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWallets(wallets: List<WalletEntity>)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET balance = balance + :delta WHERE id = :walletId")
    suspend fun updateWalletBalance(walletId: Long, delta: Double)

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int
}
