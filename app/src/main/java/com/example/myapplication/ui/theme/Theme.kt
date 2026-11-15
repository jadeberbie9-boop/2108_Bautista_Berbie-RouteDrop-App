package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = NavyDarkContainer,
    onPrimaryContainer = Color.White,
    
    secondary = NavyAccent,
    onSecondary = Color.White,
    secondaryContainer = NavySurfaceVariantDark,
    onSecondaryContainer = TextPrimaryDark,
    
    background = NavyBgDark,
    onBackground = TextPrimaryDark,
    
    surface = NavySurfaceDark,
    onSurface = TextPrimaryDark,
    
    surfaceVariant = NavySurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLightContainer,
    onPrimaryContainer = NavyPrimary,
    
    secondary = NavyAccent,
    onSecondary = Color.White,
    secondaryContainer = NavySurfaceVariantLight,
    onSecondaryContainer = TextPrimaryLight,
    
    background = NavyBgLight,
    onBackground = TextPrimaryLight,
    
    surface = NavySurfaceLight,
    onSurface = TextPrimaryLight,
    
    surfaceVariant = NavySurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    
    error = Color(0xFFDC2626)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
