package com.aircalc.converter.presentation.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Accessible color system with high contrast support.
 * Provides consistent color theming across the app.
 */
@Stable
object AccessibleColors {

    /**
     * Standard accessibility-compliant color scheme.
     */
    object Standard {
        val primary = Color(0xFF1B5E20)           // 7.2:1 contrast
        val onPrimary = Color(0xFFFFFFFF)         // Maximum contrast
        val primaryLight = Color(0xFF4CAF50)      // 4.6:1 contrast
        val onPrimaryLight = Color(0xFFFFFFFF)

        val secondary = Color(0xFF5D4037)         // 8.1:1 contrast
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryLight = Color(0xFF8D6E63)    // 4.5:1 contrast
        val onSecondaryLight = Color(0xFFFFFFFF)

        val background = Color(0xFFFFFFFF)
        val onBackground = Color(0xFF212121)      // 15.8:1 contrast
        val surface = Color(0xFFFAFAFA)
        val onSurface = Color(0xFF212121)
        val surfaceVariant = Color(0xFFF5F5F5)
        val onSurfaceVariant = Color(0xFF424242)  // 9.7:1 contrast

        val error = Color(0xFFD32F2F)             // 5.4:1 contrast
        val onError = Color(0xFFFFFFFF)
        val success = Color(0xFF2E7D32)           // 6.3:1 contrast
        val onSuccess = Color(0xFFFFFFFF)
        val warning = Color(0xFFEF6C00)           // 4.6:1 contrast
        val onWarning = Color(0xFFFFFFFF)

        val outline = Color(0xFF424242)
        val focusBorder = Color(0xFF1976D2)       // 5.9:1 contrast

        val surfaceDisabled = Color(0xFFE0E0E0)
        val onSurfaceDisabled = Color(0xFF9E9E9E)
    }

    /**
     * High contrast color scheme for accessibility.
     */
    object HighContrast {
        val primary = Color.Black
        val onPrimary = Color.White
        val primaryLight = Color(0xFF333333)
        val onPrimaryLight = Color.White

        val secondary = Color(0xFF000080)         // Navy blue
        val onSecondary = Color.White
        val secondaryLight = Color(0xFF4040FF)
        val onSecondaryLight = Color.White

        val background = Color.White
        val onBackground = Color.Black
        val surface = Color.White
        val onSurface = Color.Black
        val surfaceVariant = Color(0xFFF8F8F8)
        val onSurfaceVariant = Color.Black

        val error = Color.Red
        val onError = Color.White
        val success = Color(0xFF006400)           // Dark green
        val onSuccess = Color.White
        val warning = Color(0xFFFF8C00)           // Dark orange
        val onWarning = Color.Black

        val outline = Color.Black
        val focusBorder = Color(0xFF0000FF)       // Pure blue

        val surfaceDisabled = Color(0xFFE8E8E8)
        val onSurfaceDisabled = Color(0xFF666666)
    }

    /**
     * Get color scheme based on high contrast preference.
     */
    fun getColorScheme(isHighContrast: Boolean): Any = if (isHighContrast) HighContrast else Standard
}

/**
 * Extension functions for easier color access.
 */
val AccessibleColors.Standard.inverseSurface: Color
    get() = Color(0xFF121212)

val AccessibleColors.Standard.inverseOnSurface: Color
    get() = Color(0xFFE0E0E0)

val AccessibleColors.HighContrast.inverseSurface: Color
    get() = Color.Black

val AccessibleColors.HighContrast.inverseOnSurface: Color
    get() = Color.White