package com.example.ui.theme

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
    primary = BrandGreen,
    onPrimary = Color.Black,
    primaryContainer = BrandNavyDark,
    onPrimaryContainer = BrandGreenLight,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = BrandGreenSurface,
    onPrimaryContainer = BrandNavyDark,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = BrandGreenSurface,
    onSecondaryContainer = BrandGreenDark,
    tertiary = ElectricBlue,
    background = BackgroundLight,
    onBackground = TextPrimaryDark,
    surface = SurfaceWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = ElectricBlueSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorder,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand colors for clear readability
    content: @Composable () -> Unit,
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
