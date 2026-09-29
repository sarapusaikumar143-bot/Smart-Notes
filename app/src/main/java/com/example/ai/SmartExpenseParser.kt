package com.example.ai

import com.example.model.TransactionType
import org.json.JSONObject
import java.util.Locale

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val confidence: Float = 0.95f
)

class SmartExpenseParser(private val geminiService: GeminiAiService) {

    suspend fun parseNaturalInput(input: String): ParsedExpense {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return ParsedExpense(
                title = "Miscellaneous",
                amount = 0.0,
                type = TransactionType.EXPENSE,
                category = "Other"
            )
        }

        // Try Gemini AI first if network is up
        val geminiResult = tryGeminiParse(trimmed)
        if (geminiResult != null) {
            return geminiResult
        }

        // Highly accurate offline rule-based parser
        return parseOffline(trimmed)
    }

    private suspend fun tryGeminiParse(input: String): ParsedExpense? {
        val prompt = """
            Parse this financial entry into JSON:
            "$input"
            Respond ONLY with a JSON object in this exact format:
            {
              "title": "Clean Merchant/Item Name",
              "amount": 250.0,
              "type": "EXPENSE" or "INCOME",
              "category": "Food" | "Transport" | "Shopping" | "Bills" | "Entertainment" | "Health" | "Salary" | "Investment" | "Other"
            }
        """.trimIndent()

        val response = geminiService.generateContent(prompt)
        if (response.isSuccess) {
            val text = response.getOrNull()?.trim() ?: return null
            try {
                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                val title = json.optString("title", "Expense")
                val amount = json.optDouble("amount", 0.0)
                val typeStr = json.optString("type", "EXPENSE")
                val category = json.optString("category", "Food")
                val type = if (typeStr.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
                return ParsedExpense(title, amount, type, category, 0.99f)
            } catch (e: Exception) {
                // fallback
            }
        }
        return null
    }

    fun parseOffline(input: String): ParsedExpense {
        val lower = input.lowercase(Locale.ROOT)

        // 1. Extract Amount
        var amount = 0.0
        val numberRegex = Regex("""(?:(?:rs\.?|inr|₹|\$|€|£)\s*)?([0-9]+(?:\.[0-9]{1,2})?)\s*(k|k\b)?""", RegexOption.IGNORE_CASE)
        val match = numberRegex.find(input)
        if (match != null) {
            val numStr = match.groupValues[1]
            val isK = match.groupValues.getOrNull(2)?.equals("k", ignoreCase = true) == true
            val parsed = numStr.toDoubleOrNull() ?: 0.0
            amount = if (isK) parsed * 1000 else parsed
        }

        // 2. Detect Type (Income vs Expense)
        val incomeKeywords = listOf("salary", "credited", "received", "refund", "cashback", "dividend", "interest", "bonus", "freelance", "profit", "gift received")
        val isIncome = incomeKeywords.any { lower.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 3. Detect Category & Title
        val category: String
        val titleCandidate: String

        when {
            listOf("biryani", "pizza", "burger", "coffee", "tea", "chai", "lunch", "dinner", "breakfast", "swiggy", "zomato", "restaurant", "cafe", "food", "groceries", "fruits", "vegetables", "milk", "egg", "bakery", "snack", "sweet", "bar", "drinks").any { lower.contains(it) } -> {
                category = "Food"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            listOf("petrol", "diesel", "fuel", "uber", "ola", "cab", "taxi", "bus", "train", "metro", "auto", "flight", "toll", "parking").any { lower.contains(it) } -> {
                category = "Transport"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            listOf("electricity", "water", "wifi", "broadband", "recharge", "mobile bill", "rent", "maintenance", "gas cylinder", "netflix", "prime", "subscription", "emi").any { lower.contains(it) } -> {
                category = "Bills"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            listOf("amazon", "flipkart", "myntra", "clothes", "shirt", "shoes", "shopping", "mall", "watch", "electronics").any { lower.contains(it) } -> {
                category = "Shopping"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            listOf("movie", "cinema", "theatre", "concert", "game", "gaming", "outing", "party", "trip").any { lower.contains(it) } -> {
                category = "Entertainment"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            listOf("medicine", "doctor", "hospital", "pharmacy", "clinic", "tests", "gym", "workout", "fitness").any { lower.contains(it) } -> {
                category = "Health"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            isIncome && lower.contains("salary") -> {
                category = "Salary"
                titleCandidate = "Monthly Salary"
            }
            isIncome -> {
                category = "Investment"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
            else -> {
                category = "Other"
                titleCandidate = input.replace(numberRegex, "").trim()
            }
        }

        val cleanedTitle = titleCandidate
            .replace(Regex("""(?i)\b(add|spent|paid|for|to|at|got|on)\b"""), "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        val finalTitle = if (cleanedTitle.isBlank()) {
            if (category != "Other") category else "Quick Entry"
        } else {
            cleanedTitle
        }

        return ParsedExpense(
            title = finalTitle,
            amount = amount,
            type = type,
            category = category,
            confidence = 0.92f
        )
    }
}
