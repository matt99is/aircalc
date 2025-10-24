package com.aircalc.converter.ui.theme

import android.content.res.Configuration
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

/**
 * Manages window theme configuration for edge-to-edge display.
 * Handles device-specific workarounds (e.g., HONOR/MagicOS) for proper theme application.
 *
 * This class encapsulates complex window theming logic that was previously in MainActivity,
 * making it reusable and testable.
 */
class WindowThemeManager(private val activity: ComponentActivity) {

    /**
     * Apply window theme based on current system theme (light/dark mode).
     * Should be called both on initial creation and when configuration changes.
     */
    fun applyTheme() {
        val isDarkMode = isDarkThemeActive()
        val window = activity.window

        // Step 1: Set FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS explicitly
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (isDarkMode) {
            applyDarkTheme(window)
        } else {
            applyLightTheme()
        }
    }

    /**
     * Apply dark theme configuration to window.
     */
    private fun applyDarkTheme(window: Window) {
        // Use modern API with explicit dark scrim
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.BLACK
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.BLACK
            )
        )

        // HONOR/MagicOS workaround: Force-set colors for devices that don't respect enableEdgeToEdge
        applyDeviceSpecificWorkarounds(window, isDark = true)

        // Post to decorView to apply after view initialization
        window.decorView.post {
            applyStatusBarColors(window, isDark = true)
            applySystemBarAppearance(window, lightBars = false)
        }
    }

    /**
     * Apply light theme configuration to window.
     */
    private fun applyLightTheme() {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
    }

    /**
     * Apply device-specific workarounds for manufacturers that don't properly
     * handle standard Android theming APIs (e.g., HONOR, MagicOS).
     */
    @Suppress("DEPRECATION")
    private fun applyDeviceSpecificWorkarounds(window: Window, isDark: Boolean) {
        if (isDark) {
            // Force-set colors for devices that don't respect enableEdgeToEdge
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK
        }
    }

    /**
     * Apply status bar and navigation bar colors after view initialization.
     * This ensures colors are properly applied even after configuration changes.
     */
    @Suppress("DEPRECATION")
    private fun applyStatusBarColors(window: Window, isDark: Boolean) {
        if (isDark) {
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK
        }
    }

    /**
     * Set system bar appearance (light or dark icons/text).
     */
    private fun applySystemBarAppearance(window: Window, lightBars: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightBars
            isAppearanceLightNavigationBars = lightBars
        }
    }

    /**
     * Check if dark theme is currently active.
     */
    private fun isDarkThemeActive(): Boolean {
        val uiMode = activity.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        /**
         * Convenience method to create and apply theme in one call.
         */
        fun applyTheme(activity: ComponentActivity) {
            WindowThemeManager(activity).applyTheme()
        }
    }
}
