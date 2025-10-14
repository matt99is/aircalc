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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    secondary = DarkPrimaryGreen,  // Green for +/- buttons in dark mode
    tertiary = Color(0xFFE8E8E8),  // Neutral light gray to prevent tinting
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color(0xFFF0F2F5),  // Off-white text on green buttons
    onSecondary = Color(0xFFF0F2F5), // Off-white text on green +/- buttons
    onTertiary = Color(0xFF252525),  // Dark text on light tertiary
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onTertiaryContainer = Color(0xFFE8E8E8),  // Light gray text on dark containers
    primaryContainer = Color(0xFF2A5347),  // Darker green container
    secondaryContainer = Color(0xFF7A3735), // Darker red container
    tertiaryContainer = DarkInputBackground, // Dark purple-gray for input cards
    outline = DarkSurfaceVariant,
    error = DarkPrimaryRed,
    onError = Color(0xFFF0F2F5)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreen,  // Green for +/- buttons in light mode
    tertiary = PrimaryRed,
    background = PureWhite,  // White background for light mode
    surface = PureWhite,
    surfaceVariant = LightGray,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
    primaryContainer = PrimaryGreen80,
    secondaryContainer = PrimaryRed80,
    tertiaryContainer = PureWhite,  // White for input cards in light mode
    outline = MediumGray,
    error = PrimaryRed,
    onError = PureWhite,
    onTertiaryContainer = PureBlack  // Black text on white input cards
)

@Composable
fun AirCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Disabled to use custom colors
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
    // Status bar configuration now handled in MainActivity using enableEdgeToEdge()
    // This is more compatible with Honor/MagicOS and other manufacturer customizations

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}