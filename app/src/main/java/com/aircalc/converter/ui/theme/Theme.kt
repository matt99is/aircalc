package com.aircalc.converter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WarmOrange80,
    secondary = Terracotta80,
    tertiary = Sand80,
    background = DarkBrown,
    surface = Sand40,
    onPrimary = DarkBrown,
    onSecondary = DarkBrown,
    onTertiary = DarkBrown,
    onBackground = WarmWhite,
    onSurface = WarmWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryRed,
    tertiary = PrimaryRed,
    background = CreamBackground,
    surface = PureWhite,
    surfaceVariant = LightGray,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
    primaryContainer = PrimaryGreen80,
    secondaryContainer = PrimaryRed80,
    tertiaryContainer = LightGray,
    outline = MediumGray,
    error = PrimaryRed,
    onError = PureWhite
)

@Composable
fun AirCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}