package com.example.util

import android.content.Context
import android.content.Intent
import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.model.WalletEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun generateCsv(transactions: List<TransactionEntity>, wallets: List<WalletEntity>): String {
        val walletMap = wallets.associate { it.id to it.name }
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Type,Category,Amount,Wallet,Note\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.dateMillis))
            val walletName = walletMap[tx.walletId] ?: "Unknown"
            val cleanTitle = tx.title.replace(",", " ")
            val cleanNote = tx.note.replace(",", " ")
            sb.append("${tx.id},\"$dateStr\",\"$cleanTitle\",${tx.type},\"${tx.category}\",${tx.amount},\"$walletName\",\"$cleanNote\"\n")
        }
        return sb.toString()
    }

    fun generateFormattedReport(
        transactions: List<TransactionEntity>,
        wallets: List<WalletEntity>,
        periodTitle: String = "Financial Report"
    ): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val categoryBreakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("   AI MONEY + SMART NOTES REPORT        \n")
        sb.append("   $periodTitle                          \n")
        sb.append("   Generated: ${dateFormat.format(Date())}\n")
        sb.append("=========================================\n\n")

        sb.append("SUMMARY:\n")
        sb.append("• Total Income:   ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}\n")
        sb.append("• Total Expenses: ₹${String.format(Locale.getDefault(), "%,.2f", totalExpense)}\n")
        sb.append("• Net Surplus:    ₹${String.format(Locale.getDefault(), "%,.2f", netBalance)}\n\n")

        sb.append("WALLETS CURRENT BALANCE:\n")
        for (w in wallets) {
            sb.append("• ${w.name}: ₹${String.format(Locale.getDefault(), "%,.2f", w.balance)}\n")
        }
        sb.append("\n")

        sb.append("EXPENSES BY CATEGORY:\n")
        for ((cat, amt) in categoryBreakdown.entries.sortedByDescending { it.value }) {
            val pct = if (totalExpense > 0) (amt / totalExpense * 100).toInt() else 0
            sb.append("• $cat: ₹${String.format(Locale.getDefault(), "%,.2f", amt)} ($pct%)\n")
        }
        sb.append("\n")

        sb.append("TRANSACTION HISTORY (${transactions.size} records):\n")
        sb.append("-----------------------------------------\n")
        for (tx in transactions.take(25)) {
            val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
            sb.append("${dateFormat.format(Date(tx.dateMillis))} | ${tx.title.take(20).padEnd(20)} | $sign₹${tx.amount.toInt()} [${tx.category}]\n")
        }
        if (transactions.size > 25) {
            sb.append("... and ${transactions.size - 25} more transactions.\n")
        }
        sb.append("=========================================\n")
        return sb.toString()
    }

    fun shareReport(context: Context, text: String, title: String = "Share Financial Report") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }
}
