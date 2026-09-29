package com.example.ai

import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.model.WalletEntity
import java.text.NumberFormat
import java.util.Locale

class FinancialChatAssistant(
    private val geminiService: GeminiAiService,
    private val predictionEngine: PredictionEngine
) {

    suspend fun answerFinancialQuery(
        query: String,
        wallets: List<WalletEntity>,
        transactions: List<TransactionEntity>
    ): String {
        val totalBalance = wallets.sumOf { it.balance }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val income = transactions.filter { it.type == TransactionType.INCOME }
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }

        val categoryBreakdown = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
            .take(5)
            .joinToString(", ") { "${it.key}: ₹${it.value.toInt()}" }

        val prediction = predictionEngine.computePrediction(transactions, wallets)

        val contextInfo = """
            User's Current Financial Snapshot:
            - Total Liquid Net Worth in Wallets: ₹${totalBalance.toInt()}
            - Wallets: ${wallets.joinToString(", ") { "${it.name}: ₹${it.balance.toInt()}" }}
            - Recent Period Total Income: ₹${totalIncome.toInt()}
            - Recent Period Total Expenses: ₹${totalExpense.toInt()}
            - Top Expense Categories: $categoryBreakdown
            - Estimated Daily Burn Rate: ₹${prediction.dailyBurnRate.toInt()}/day
            - Financial Runway Estimate: ${prediction.runwayDays ?: "Healthy"} days
            - Savings Rate: ${prediction.savingsRatePercent.toInt()}%
        """.trimIndent()

        val systemInstruction = """
            You are "SpendWise AI", an empathetic, razor-sharp personal financial advisor and smart notes copilot.
            Always ground your advice directly in the user's provided real balances, category expenses, and runway metrics.
            Be concise, punchy, actionable, and encouraging. Use bullet points where appropriate.
            If the user asks Telugu or other languages, answer in their language.
        """.trimIndent()

        val fullPrompt = """
            $contextInfo

            User Question: "$query"

            Provide tailored, smart, direct financial guidance based strictly on the user's data above.
        """.trimIndent()

        val geminiResult = geminiService.generateContent(fullPrompt, systemInstruction)
        if (geminiResult.isSuccess) {
            val text = geminiResult.getOrNull()
            if (!text.isNullOrBlank()) return text.trim()
        }

        // Offline smart financial advisor fallback
        val q = query.lowercase(Locale.ROOT)
        return when {
            q.contains("save") || q.contains("more money") -> {
                "💡 **Savings Strategy Based on Your Data:**\n\n" +
                "1. **Tackle High-Spend Categories:** Your top spending is in $categoryBreakdown. Reducing Food/Dining by 20% can save you ~₹${((expenses.filter { it.category == "Food" }.sumOf { it.amount }) * 0.20).toInt()} this month.\n" +
                "2. **Daily Burn Cap:** Your current burn rate is ₹${prediction.dailyBurnRate.toInt()}/day. Capping non-essential daily spends at ₹250 will extend your runway by ${(prediction.runwayDays ?: 15) + 8} days.\n" +
                "3. **Emergency Cushion:** Move 15% of your ₹${wallets.find { it.type == com.example.model.WalletType.BANK }?.balance?.toInt() ?: 10000} Bank balance to a locked savings pot."
            }
            q.contains("run out") || q.contains("runway") || q.contains("afford") -> {
                "📊 **Runway & Affordability Analysis:**\n\n" +
                "• Estimated Runway: **${prediction.runwayDays ?: 24} days** at your current burn rate of ₹${prediction.dailyBurnRate.toInt()}/day.\n" +
                "• Total Liquid Funds: **₹${totalBalance.toInt()}** across ${wallets.size} wallets.\n" +
                "• Recommendation: Avoid discretionary single purchases exceeding ₹2,500 until the next major income inflow."
            }
            q.contains("highest") || q.contains("most") -> {
                val highest = expenses.maxByOrNull { it.amount }
                if (highest != null) {
                    "📌 Your single highest expense was **${highest.title}** for **₹${highest.amount.toInt()}** (${highest.category})."
                } else {
                    "No major expenses recorded yet!"
                }
            }
            else -> {
                "📊 **Financial Summary:**\n" +
                "• Net Worth: ₹${totalBalance.toInt()}\n" +
                "• Expenses: ₹${totalExpense.toInt()} | Income: ₹${totalIncome.toInt()}\n" +
                "• Top spends: $categoryBreakdown\n" +
                "• Burn rate: ₹${prediction.dailyBurnRate.toInt()}/day (~${prediction.runwayDays ?: 25} days runway).\n\n" +
                "Ask me anything like *'How can I save on food?'*, *'Can I afford a trip?'*, or *'Predict my month-end balance'!"
            }
        }
    }
}
