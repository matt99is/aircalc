package com.aircalc.converter.presentation.state

import androidx.compose.runtime.Stable
import com.aircalc.converter.domain.model.ConversionResult
import com.aircalc.converter.domain.model.FoodCategory
import com.aircalc.converter.domain.model.TemperatureUnit

/**
 * UI state for the Air Fryer app.
 * Represents all the state needed for the UI layer.
 */
@Stable
data class AirFryerUiState(
    // Input state
    val ovenTemperature: Int = 350,
    val cookingTime: Int = 25,
    val selectedCategory: FoodCategory? = FoodCategory.FROZEN_FOODS,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,

    // Conversion state
    val isConverting: Boolean = false,
    val conversionResult: ConversionResult? = null,

    // Error handling
    val errorMessage: String? = null,

    // Accessibility
    val accessibilityAnnouncement: String = "",
    val announcementId: Int = 0,

    // UI preferences
    val isHighContrastMode: Boolean = false,
    val textScaleFactor: Float = 1.0f
) {
    /**
     * Check if we have a valid configuration for conversion.
     */
    fun isReadyForConversion(): Boolean {
        return selectedCategory != null &&
                temperatureUnit.isValidTemperature(ovenTemperature) &&
                cookingTime in 1..300 &&
                !isConverting
    }

    /**
     * Check if we have conversion results to display.
     */
    fun hasResults(): Boolean = conversionResult != null

    /**
     * Check if there's an error to display.
     */
    fun hasError(): Boolean = errorMessage != null

    /**
     * Get formatted temperature with unit.
     */
    fun getFormattedTemperature(): String = "$ovenTemperature${temperatureUnit.symbol}"

    /**
     * Get formatted cooking time.
     */
    fun getFormattedTime(): String = "$cookingTime min"
}

/**
 * Sealed class representing different UI states for better state management.
 */
sealed class ConversionState {
    object Idle : ConversionState()
    object Converting : ConversionState()
    data class Success(val result: ConversionResult) : ConversionState()
    data class Error(val message: String) : ConversionState()
}

/**
 * Input validation state for real-time feedback.
 */
@Stable
data class InputValidationState(
    val temperatureError: String? = null,
    val timeError: String? = null,
    val categoryError: String? = null,
    val isValid: Boolean = true
) {
    companion object {
        fun validate(
            temperature: Int,
            time: Int,
            category: FoodCategory?,
            unit: TemperatureUnit
        ): InputValidationState {
            @Suppress("UNUSED_VARIABLE")
            val errors = mutableListOf<String>()
            var tempError: String? = null
            var timeError: String? = null
            var categoryError: String? = null

            // Validate temperature
            if (!unit.isValidTemperature(temperature)) {
                tempError = when (unit) {
                    TemperatureUnit.FAHRENHEIT -> "Temperature must be between 200°F and 500°F"
                    TemperatureUnit.CELSIUS -> "Temperature must be between 93°C and 260°C"
                }
            }

            // Validate time
            if (time < 1) {
                timeError = "Cooking time must be at least 1 minute"
            } else if (time > 300) {
                timeError = "Cooking time cannot exceed 5 hours"
            }

            // Validate category
            if (category == null) {
                categoryError = "Please select a food category"
            }

            return InputValidationState(
                temperatureError = tempError,
                timeError = timeError,
                categoryError = categoryError,
                isValid = tempError == null && timeError == null && categoryError == null
            )
        }
    }
}