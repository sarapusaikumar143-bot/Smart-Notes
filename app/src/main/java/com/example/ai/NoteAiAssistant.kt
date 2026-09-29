package com.example.ai

import org.json.JSONArray
import org.json.JSONObject

class NoteAiAssistant(
    private val geminiService: GeminiAiService,
    private val expenseParser: SmartExpenseParser
) {

    suspend fun summarizeNote(content: String): String {
        if (content.isBlank()) return "Note is empty."

        val prompt = "Summarize this note in 2-3 concise bullet points or key takeaways:\n\n$content"
        val result = geminiService.generateContent(prompt)
        if (result.isSuccess) {
            val text = result.getOrNull()
            if (!text.isNullOrBlank()) return text.trim()
        }

        // Offline fallback
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.size <= 2) return content
        return "• " + lines.take(3).joinToString("\n• ") { it.take(100) }
    }

    suspend fun convertToChecklist(content: String): List<Pair<String, Boolean>> {
        if (content.isBlank()) return emptyList()

        val prompt = """
            Convert the following notes into an actionable checklist.
            Return ONLY a valid JSON array of objects with keys "text" (string) and "done" (boolean).
            Example: [{"text": "Buy groceries", "done": false}]
            Notes content:
            $content
        """.trimIndent()

        val result = geminiService.generateContent(prompt)
        if (result.isSuccess) {
            val text = result.getOrNull() ?: ""
            try {
                val clean = text.substringAfter("[").substringBeforeLast("]")
                val arr = JSONArray("[$clean]")
                val list = mutableListOf<Pair<String, Boolean>>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Pair(obj.optString("text"), obj.optBoolean("done", false)))
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                // fallback
            }
        }

        // Offline fallback
        return content.lines()
            .map { it.replace(Regex("""^[-*•\d.]\s*"""), "").trim() }
            .filter { it.isNotBlank() }
            .map { Pair(it, false) }
    }

    suspend fun translateNote(content: String, targetLanguage: String): String {
        if (content.isBlank()) return ""

        val prompt = "Translate the following text accurately into $targetLanguage while preserving bullet points and numbers:\n\n$content"
        val result = geminiService.generateContent(prompt)
        if (result.isSuccess) {
            val text = result.getOrNull()
            if (!text.isNullOrBlank()) return text.trim()
        }

        // Offline notification
        return "[$targetLanguage Translation (AI requires active GEMINI_API_KEY)]:\n$content"
    }

    suspend fun detectExpenseInNote(content: String): ParsedExpense? {
        val expense = expenseParser.parseNaturalInput(content)
        return if (expense.amount > 0) expense else null
    }
}
