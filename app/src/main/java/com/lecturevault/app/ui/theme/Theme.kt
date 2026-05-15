package com.lecturevault.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF7C3AED),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    secondary = Color(0xFFA78BFA),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
)

@Composable
fun LectureVaultTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
