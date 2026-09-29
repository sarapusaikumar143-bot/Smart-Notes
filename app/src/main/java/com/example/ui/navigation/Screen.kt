package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Calendar : Screen("calendar", "Calendar")
    object Analytics : Screen("analytics", "Analytics")
    object AI : Screen("ai", "AI Copilot")
    object Notes : Screen("notes", "Notes")
    object Wallet : Screen("wallet", "Wallets")
    object Files : Screen("files", "Files")
    object AddTransaction : Screen("add_transaction", "Add Entry")
}
