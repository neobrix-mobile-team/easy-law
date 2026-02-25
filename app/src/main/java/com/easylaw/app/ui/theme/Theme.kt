package com.easylaw.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 컬러 팔레트 정의
private val LightColorScheme =
    lightColorScheme(
        // Deep Blue
        primary = Color(0xFF1F5B9C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E7FF),
        onPrimaryContainer = Color(0xFF001A47),
        // Soft Grey
        secondary = Color(0xFF8C9DB0),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCFE0F1),
        onSecondaryContainer = Color(0xFF1C2C3C),
        // Amber
        tertiary = Color(0xFFC29B00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDEB8),
        onTertiaryContainer = Color(0xFF3D2800),
        error = Color(0xFFB3261E),
        onError = Color.White,
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        background = Color(0xFFFBFCFE),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFBFCFE),
        onSurface = Color(0xFF1A1C1E),
    )

private val DarkColorScheme =
    darkColorScheme(
        // Light Blue
        primary = Color(0xFFAFCDFF),
        onPrimary = Color(0xFF003166),
        primaryContainer = Color(0xFF004797),
        onPrimaryContainer = Color(0xFFD6E7FF),
        // Light Grey
        secondary = Color(0xFFB3C5D8),
        onSecondary = Color(0xFF304154),
        secondaryContainer = Color(0xFF475C6E),
        onSecondaryContainer = Color(0xFFCFE0F1),
        // Light Amber
        tertiary = Color(0xFFE5B800),
        onTertiary = Color(0xFF664D00),
        tertiaryContainer = Color(0xFF986F00),
        onTertiaryContainer = Color(0xFFFFDEB8),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF1A1C1E),
        onBackground = Color(0xFFE2E2E5),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE2E2E5),
    )

@Composable
@Suppress("ktlint:standard:function-naming") // 이 줄 추가
fun EasyLawTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
