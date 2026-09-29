package com.example.domain.models

enum class WalletType {
    CASH, BANK, UPI, CARD, SAVINGS
}

data class Wallet(
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val balance: Double,
    val colorHex: String,
    val iconName: String,
    val isDefault: Boolean = false
)

data class ChecklistItem(
    val id: String,
    val text: String,
    val done: Boolean
)

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val linkedTransactionId: Long? = null,
    val category: String = "General",
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DailyInsight(
    val title: String,
    val description: String,
    val badge: String,
    val changePercent: Double? = null,
    val isPositive: Boolean = true
)

data class FinancialPrediction(
    val runwayDays: Int?,
    val dailyBurnRate: Double,
    val projectedMonthEndBalance: Double,
    val overspendingAlert: String?,
    val savingsRatePercent: Double,
    val summaryText: String
)
