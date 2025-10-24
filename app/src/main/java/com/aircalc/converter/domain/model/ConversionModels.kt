package com.aircalc.converter.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain models for conversion operations.
 * These models contain no UI or framework dependencies.
 */

/**
 * Input parameters for air fryer conversion.
 * Validation is handled by ConversionValidator, not in the data class itself.
 */
@Immutable
data class ConversionInput(
    val ovenTemperature: Int,
    val cookingTimeMinutes: Int,
    val foodCategory: FoodCategory,
    val temperatureUnit: TemperatureUnit
)

/**
 * Result of air fryer conversion with all calculated values.
 */
@Immutable
data class ConversionResult(
    val originalTemperature: Int,
    val originalTime: Int,
    val airFryerTemperature: Int,
    val airFryerTimeMinutes: Int,
    val temperatureUnit: TemperatureUnit,
    val foodCategory: FoodCategory,
    val cookingTip: String,
    val temperatureReduction: Int,
    val timeReduction: Int
) {
    /**
     * Get formatted temperature with unit symbol.
     */
    fun getFormattedAirFryerTemp(): String = "$airFryerTemperature${temperatureUnit.symbol}"

    /**
     * Get formatted original temperature with unit symbol.
     */
    fun getFormattedOriginalTemp(): String = "$originalTemperature${temperatureUnit.symbol}"

    /**
     * Get time savings in minutes.
     */
    fun getTimeSavings(): Int = originalTime - airFryerTimeMinutes

    /**
     * Get percentage time reduction.
     */
    fun getTimeReductionPercentage(): Int =
        ((originalTime - airFryerTimeMinutes).toDouble() / originalTime * 100).toInt()
}

/**
 * Validation result for conversion input.
 */
sealed class ConversionValidation {
    object Valid : ConversionValidation()

    data class Invalid(
        val errors: List<ValidationError>
    ) : ConversionValidation()

    fun isValid(): Boolean = this is Valid
}

/**
 * Specific validation errors.
 */
sealed class ValidationError(val message: String) {
    object TemperatureTooLow : ValidationError("Temperature is too low for safe cooking")
    object TemperatureTooHigh : ValidationError("Temperature is too high for this appliance")
    object TimeTooShort : ValidationError("Cooking time is too short")
    object TimeTooLong : ValidationError("Cooking time is unusually long")
    data class Custom(val customMessage: String) : ValidationError(customMessage)
}