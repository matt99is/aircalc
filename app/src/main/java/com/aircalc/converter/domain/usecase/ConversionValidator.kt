package com.aircalc.converter.domain.usecase

import com.aircalc.converter.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validator for conversion input parameters.
 * Contains all business rules and validation logic.
 */
@Singleton
class ConversionValidator @Inject constructor() {

    /**
     * Validate conversion input against all business rules.
     */
    fun validateInput(input: ConversionInput): ConversionValidation {
        val errors = mutableListOf<ValidationError>()

        // Validate temperature
        validateTemperature(input.ovenTemperature, input.temperatureUnit)?.let {
            errors.add(it)
        }

        // Validate cooking time
        validateCookingTime(input.cookingTimeMinutes)?.let {
            errors.add(it)
        }

        // Validate food category compatibility
        validateFoodCategoryCompatibility(input.foodCategory, input.ovenTemperature, input.temperatureUnit)?.let {
            errors.add(it)
        }

        return if (errors.isEmpty()) {
            ConversionValidation.Valid
        } else {
            ConversionValidation.Invalid(errors)
        }
    }

    /**
     * Validate temperature range and safety.
     */
    private fun validateTemperature(temperature: Int, unit: TemperatureUnit): ValidationError? {
        return when {
            !unit.isValidTemperature(temperature) -> {
                when (unit) {
                    TemperatureUnit.FAHRENHEIT -> when {
                        temperature < 200 -> ValidationError.TemperatureTooLow
                        temperature > 500 -> ValidationError.TemperatureTooHigh
                        else -> null
                    }
                    TemperatureUnit.CELSIUS -> when {
                        temperature < 93 -> ValidationError.TemperatureTooLow
                        temperature > 260 -> ValidationError.TemperatureTooHigh
                        else -> null
                    }
                }
            }
            else -> null
        }
    }

    /**
     * Validate cooking time range.
     */
    private fun validateCookingTime(timeMinutes: Int): ValidationError? {
        return when {
            timeMinutes < 1 -> ValidationError.TimeTooShort
            timeMinutes > 300 -> ValidationError.TimeTooLong // 5 hours max
            else -> null
        }
    }

    /**
     * Validate food category compatibility with temperature and settings.
     */
    private fun validateFoodCategoryCompatibility(
        category: FoodCategory,
        temperature: Int,
        unit: TemperatureUnit
    ): ValidationError? {
        // Convert temperature to Fahrenheit for comparison
        val tempInFahrenheit = if (unit == TemperatureUnit.CELSIUS) {
            unit.convertTo(temperature, TemperatureUnit.FAHRENHEIT)
        } else {
            temperature
        }

        return when (category.id) {
            "fresh_vegetables" -> {
                if (tempInFahrenheit < 300) {
                    ValidationError.Custom("Temperature too low for vegetables - may not cook properly")
                } else null
            }
            "raw_meats" -> {
                if (tempInFahrenheit < 325) {
                    ValidationError.Custom("Temperature too low for raw meat - food safety concern")
                } else null
            }
            else -> null
        }
    }

    /**
     * Check if the conversion result is reasonable.
     */
    fun validateResult(result: ConversionResult): ConversionValidation {
        val errors = mutableListOf<ValidationError>()

        // Check if air fryer temperature is reasonable
        if (result.airFryerTemperature < 200 && result.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            errors.add(ValidationError.Custom("Resulting air fryer temperature is too low"))
        }

        if (result.airFryerTemperature < 93 && result.temperatureUnit == TemperatureUnit.CELSIUS) {
            errors.add(ValidationError.Custom("Resulting air fryer temperature is too low"))
        }

        // Check if time reduction is reasonable
        if (result.airFryerTimeMinutes < 1) {
            errors.add(ValidationError.Custom("Resulting cooking time is too short"))
        }

        return if (errors.isEmpty()) {
            ConversionValidation.Valid
        } else {
            ConversionValidation.Invalid(errors)
        }
    }
}