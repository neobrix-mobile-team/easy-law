package com.neobrix.easylaw.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 컬러 팔레트 정의
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F5B9C), // Deep Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E7FF),
    onPrimaryContainer = Color(0xFF001A47),
    
    secondary = Color(0xFF8C9DB0), // Soft Grey
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE0F1),
    onSecondaryContainer = Color(0xFF1C2C3C),
    
    tertiary = Color(0xFFC29B00), // Amber
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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAFCDFF), // Light Blue
    onPrimary = Color(0xFF003166),
    primaryContainer = Color(0xFF004797),
    onPrimaryContainer = Color(0xFFD6E7FF),
    
    secondary = Color(0xFFB3C5D8), // Light Grey
    onSecondary = Color(0xFF304154),
    secondaryContainer = Color(0xFF475C6E),
    onSecondaryContainer = Color(0xFFCFE0F1),
    
    tertiary = Color(0xFFE5B800), // Light Amber
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
fun EasyLawTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
