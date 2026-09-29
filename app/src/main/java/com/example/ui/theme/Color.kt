package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palette per specifications
val DarkNavy = Color(0xFF0F172A)
val DeepBackgroundDark = Color(0xFF020617)
val LightBackground = Color(0xFFF8FAFC)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E293B)

val MoneyGreen = Color(0xFF22C55E)
val MoneyGreenDark = Color(0xFF16A34A)
val MoneyGreenSoft = Color(0xFFDCFCE7)

val ElectricBlue = Color(0xFF3B82F6)
val ElectricBlueDark = Color(0xFF2563EB)
val ElectricBlueSoft = Color(0xFFDBEAFE)

val DangerRed = Color(0xFFEF4444)
val DangerRedSoft = Color(0xFFFEE2E2)

val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, MoneyGreen)
)

val DarkHeaderGradient = Brush.verticalGradient(
    colors = listOf(DarkNavy, Color(0xFF1E293B))
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF38BDF8), Color(0xFF22C55E))
)

val DangerGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF87171), DangerRed)
)
