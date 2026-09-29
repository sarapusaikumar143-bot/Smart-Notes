package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val isChecklist: Boolean = false,
    val checklistJson: String = "[]",
    val linkedTransactionId: Long? = null,
    val category: String = "General",
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
