package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Slate800,
    onPrimaryContainer = Color.White,
    secondary = MoneyGreen,
    onSecondary = DarkNavy,
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = MoneyGreenSoft,
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = DeepBackgroundDark,
    onBackground = Slate100,
    surface = DarkNavy,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    error = DangerRed,
    onError = Color.White,
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = DarkNavy,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = DarkNavy,
    secondary = MoneyGreenDark,
    onSecondary = Color.White,
    secondaryContainer = MoneyGreenSoft,
    onSecondaryContainer = Color(0xFF14532D),
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = DarkNavy,
    surface = CardLight,
    onSurface = DarkNavy,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    error = DangerRed,
    onError = Color.White,
    outline = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted pro palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
