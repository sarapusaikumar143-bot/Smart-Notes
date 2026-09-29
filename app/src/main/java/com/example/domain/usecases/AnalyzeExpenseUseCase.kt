package com.example.domain.usecases

import com.example.domain.models.Category
import com.example.domain.models.Transaction
import com.example.domain.models.TransactionType

data class ParsedExpenseResult(
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category
)

class AnalyzeExpenseUseCase {
    operator fun invoke(text: String): ParsedExpenseResult {
        val lower = text.lowercase()

        // Extract amount
        var amount = 0.0
        val regex = Regex("""(?:(?:rs\.?|inr|₹|\$|€|£)\s*)?([0-9]+(?:\.[0-9]{1,2})?)\s*(k|k\b)?""", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        if (match != null) {
            val num = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val isK = match.groupValues.getOrNull(2)?.equals("k", ignoreCase = true) == true
            amount = if (isK) num * 1000 else num
        }

        // Determine income or expense
        val isIncome = listOf("salary", "credited", "received", "refund", "cashback", "bonus", "dividend").any { lower.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // Determine Category per user specification:
        // text.contains("biryani") -> Category.FOOD
        // text.contains("petrol") -> Category.TRANSPORT (FUEL)
        val category: Category = when {
            lower.contains("biryani") || lower.contains("pizza") || lower.contains("burger") ||
            lower.contains("coffee") || lower.contains("chai") || lower.contains("lunch") ||
            lower.contains("dinner") || lower.contains("food") || lower.contains("groceries") -> Category.FOOD

            lower.contains("petrol") || lower.contains("diesel") || lower.contains("fuel") ||
            lower.contains("uber") || lower.contains("ola") || lower.contains("cab") ||
            lower.contains("bus") || lower.contains("metro") -> Category.TRANSPORT

            lower.contains("electricity") || lower.contains("recharge") || lower.contains("wifi") ||
            lower.contains("bill") || lower.contains("rent") || lower.contains("subscription") -> Category.BILLS

            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("clothes") ||
            lower.contains("shoes") || lower.contains("shopping") -> Category.SHOPPING

            lower.contains("movie") || lower.contains("cinema") || lower.contains("concert") ||
            lower.contains("game") -> Category.ENTERTAINMENT

            lower.contains("medicine") || lower.contains("doctor") || lower.contains("hospital") ||
            lower.contains("gym") -> Category.HEALTH

            isIncome && lower.contains("salary") -> Category.SALARY
            isIncome -> Category.INVESTMENT
            else -> Category.OTHER
        }

        val cleanedTitle = text.replace(regex, "")
            .replace(Regex("""(?i)\b(add|spent|paid|for|to|at|got|on)\b"""), "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        val finalTitle = if (cleanedTitle.isBlank()) category.displayName else cleanedTitle

        return ParsedExpenseResult(
            title = finalTitle,
            amount = amount,
            type = type,
            category = category
        )
    }
}
