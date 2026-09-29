package com.example.ai

import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.model.WalletEntity
import java.util.Calendar

data class DailyInsight(
    val title: String,
    val description: String,
    val badge: String,
    val changePercent: Double?, // e.g. +35.0 or -12.0
    val isPositive: Boolean
)

data class FinancialPrediction(
    val runwayDays: Int?, // days until funds deplete at current rate
    val dailyBurnRate: Double,
    val projectedMonthEndBalance: Double,
    val overspendingAlert: String?,
    val savingsRatePercent: Double,
    val summaryText: String
)

class PredictionEngine {

    fun generateDailyInsight(
        transactions: List<TransactionEntity>,
        targetDateMillis: Long = System.currentTimeMillis()
    ): DailyInsight {
        val cal = Calendar.getInstance()
        cal.timeInMillis = targetDateMillis
        val targetDay = cal.get(Calendar.DAY_OF_YEAR)
        val targetYear = cal.get(Calendar.YEAR)

        val calYesterday = Calendar.getInstance()
        calYesterday.timeInMillis = targetDateMillis
        calYesterday.add(Calendar.DAY_OF_YEAR, -1)
        val yestDay = calYesterday.get(Calendar.DAY_OF_YEAR)
        val yestYear = calYesterday.get(Calendar.YEAR)

        var todayExpense = 0.0
        var yestExpense = 0.0
        val categoryExpenses = mutableMapOf<String, Double>()

        for (tx in transactions) {
            val txCal = Calendar.getInstance()
            txCal.timeInMillis = tx.dateMillis
            val txDay = txCal.get(Calendar.DAY_OF_YEAR)
            val txYear = txCal.get(Calendar.YEAR)

            if (tx.type == TransactionType.EXPENSE) {
                if (txDay == targetDay && txYear == targetYear) {
                    todayExpense += tx.amount
                    categoryExpenses[tx.category] = (categoryExpenses[tx.category] ?: 0.0) + tx.amount
                } else if (txDay == yestDay && txYear == yestYear) {
                    yestExpense += tx.amount
                }
            }
        }

        if (todayExpense == 0.0) {
            return DailyInsight(
                title = "Zero Spend Day!",
                description = "Awesome discipline. Your wallet is taking a well-deserved rest today.",
                badge = "Streak Safe 🌟",
                changePercent = null,
                isPositive = true
            )
        }

        val topCategory = categoryExpenses.maxByOrNull { it.value }
        val topCategoryPct = if (topCategory != null && todayExpense > 0) {
            (topCategory.value / todayExpense * 100).toInt()
        } else 0

        if (yestExpense > 0.0) {
            val diff = todayExpense - yestExpense
            val pct = (diff / yestExpense) * 100.0
            if (pct > 0) {
                return DailyInsight(
                    title = "Daily Insight AI",
                    description = "You spent ${pct.toInt()}% more than yesterday. ${topCategory?.key ?: "Spending"} accounted for $topCategoryPct% of today's total.",
                    badge = "Spend Alert",
                    changePercent = pct,
                    isPositive = false
                )
            } else {
                val savingsPct = kotlin.math.abs(pct).toInt()
                return DailyInsight(
                    title = "Daily Insight AI",
                    description = "Great job! You cut spending by $savingsPct% compared to yesterday.",
                    badge = "Savings Win",
                    changePercent = pct,
                    isPositive = true
                )
            }
        } else {
            return DailyInsight(
                title = "Daily Insight AI",
                description = "Total spent today is ${todayExpense.toInt()}. Top category: ${topCategory?.key ?: "Food"} ($topCategoryPct%).",
                badge = "Active Day",
                changePercent = null,
                isPositive = true
            )
        }
    }

    fun computePrediction(
        transactions: List<TransactionEntity>,
        wallets: List<WalletEntity>
    ): FinancialPrediction {
        val totalLiquidFunds = wallets.filter { it.type != com.example.model.WalletType.CARD }.sumOf { it.balance }

        // Past 14 days burn rate
        val fourteenDaysAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000)
        val recentExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.dateMillis >= fourteenDaysAgo
        }
        val totalRecentExpense = recentExpenses.sumOf { it.amount }
        val dailyBurn = if (totalRecentExpense > 0) totalRecentExpense / 14.0 else 350.0

        val runwayDays = if (dailyBurn > 0 && totalLiquidFunds > 0) {
            (totalLiquidFunds / dailyBurn).toInt()
        } else null

        // This month income vs expense
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (daysInMonth - dayOfMonth).coerceAtLeast(1)

        val monthTransactions = transactions.filter {
            val c = Calendar.getInstance()
            c.timeInMillis = it.dateMillis
            c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }

        val monthIncome = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthExpense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val projectedExpense = monthExpense + (dailyBurn * remainingDays)
        val projectedMonthEndBalance = monthIncome - projectedExpense

        val savingsRate = if (monthIncome > 0) {
            ((monthIncome - monthExpense) / monthIncome * 100.0).coerceAtLeast(0.0)
        } else 0.0

        var overspendingAlert: String? = null
        // Check food or transport dominance
        val categoryExpenses = monthTransactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val foodExpense = categoryExpenses["Food"] ?: 0.0
        if (monthExpense > 0 && (foodExpense / monthExpense) > 0.40) {
            overspendingAlert = "You overspent on Food this month (accounted for ${(foodExpense / monthExpense * 100).toInt()}% of expenses)."
        }

        val summaryText = if (runwayDays != null && runwayDays < 20) {
            "🔥 Critical Runway: At this daily burn rate (₹${dailyBurn.toInt()}/day), your current funds will deplete in ~$runwayDays days!"
        } else if (projectedMonthEndBalance > 0) {
            "✨ Steady Pace: Projected month-end surplus of ₹${projectedMonthEndBalance.toInt()}. Savings rate at ${savingsRate.toInt()}%."
        } else {
            "⚠️ Deficit Warning: Projected deficit of ₹${kotlin.math.abs(projectedMonthEndBalance).toInt()} by month end at current burn rate."
        }

        return FinancialPrediction(
            runwayDays = runwayDays,
            dailyBurnRate = dailyBurn,
            projectedMonthEndBalance = projectedMonthEndBalance,
            overspendingAlert = overspendingAlert,
            savingsRatePercent = savingsRate,
            summaryText = summaryText
        )
    }
}
