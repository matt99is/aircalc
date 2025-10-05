package com.aircalc.converter.domain.model

/**
 * Domain model for temperature units.
 * Contains conversion logic and validation.
 */
enum class TemperatureUnit(
    val symbol: String,
    val displayName: String
) {
    FAHRENHEIT("°F", "Fahrenheit"),
    CELSIUS("°C", "Celsius");

    /**
     * Convert temperature from this unit to the other unit.
     */
    fun convertTo(temperature: Int, targetUnit: TemperatureUnit): Int {
        if (this == targetUnit) return temperature

        return when (this) {
            FAHRENHEIT -> when (targetUnit) {
                CELSIUS -> ((temperature - 32) * 5.0 / 9.0).toInt()
                FAHRENHEIT -> temperature
            }
            CELSIUS -> when (targetUnit) {
                FAHRENHEIT -> ((temperature * 9.0 / 5.0) + 32).toInt()
                CELSIUS -> temperature
            }
        }
    }

    /**
     * Convert temperature reduction from Fahrenheit to this unit.
     */
    fun convertTempReduction(fahrenheitReduction: Int): Int {
        return when (this) {
            FAHRENHEIT -> fahrenheitReduction
            CELSIUS -> (fahrenheitReduction / 1.8).toInt()
        }
    }

    /**
     * Validate temperature range for this unit.
     */
    fun isValidTemperature(temperature: Int): Boolean {
        return when (this) {
            FAHRENHEIT -> temperature in 200..500
            CELSIUS -> temperature in 93..260 // Equivalent range in Celsius
        }
    }

    companion object {
        fun getDefault(): TemperatureUnit = FAHRENHEIT
    }
}