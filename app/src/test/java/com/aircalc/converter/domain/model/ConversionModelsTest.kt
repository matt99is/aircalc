package com.aircalc.converter.domain.model

import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.Assert.assertThrows

/**
 * Unit tests for conversion domain models.
 * Tests data classes, validation, and computed properties.
 */
class ConversionModelsTest {

    @Test
    fun `ConversionInput validates positive cooking time in constructor`() {
        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 375,
                cookingTimeMinutes = 0, // Invalid: not positive
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.FAHRENHEIT
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 375,
                cookingTimeMinutes = -5, // Invalid: negative
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.FAHRENHEIT
            )
        }
    }

    @Test
    fun `ConversionInput validates temperature range in constructor`() {
        // Invalid: too low for Fahrenheit
        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 199,
                cookingTimeMinutes = 25,
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.FAHRENHEIT
            )
        }

        // Invalid: too high for Fahrenheit
        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 501,
                cookingTimeMinutes = 25,
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.FAHRENHEIT
            )
        }

        // Invalid: too low for Celsius
        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 92,
                cookingTimeMinutes = 25,
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.CELSIUS
            )
        }

        // Invalid: too high for Celsius
        assertThrows(IllegalArgumentException::class.java) {
            ConversionInput(
                ovenTemperature = 261,
                cookingTimeMinutes = 25,
                foodCategory = FoodCategory.FROZEN_FOODS,
                temperatureUnit = TemperatureUnit.CELSIUS
            )
        }
    }

    @Test
    fun `ConversionInput accepts valid parameters`() {
        // Valid Fahrenheit input
        val fahrenheitInput = ConversionInput(
            ovenTemperature = 375,
            cookingTimeMinutes = 25,
            foodCategory = FoodCategory.FROZEN_FOODS,
            temperatureUnit = TemperatureUnit.FAHRENHEIT
        )
        assertThat(fahrenheitInput.ovenTemperature).isEqualTo(375)

        // Valid Celsius input
        val celsiusInput = ConversionInput(
            ovenTemperature = 190,
            cookingTimeMinutes = 30,
            foodCategory = FoodCategory.FRESH_VEGETABLES,
            temperatureUnit = TemperatureUnit.CELSIUS
        )
        assertThat(celsiusInput.ovenTemperature).isEqualTo(190)
    }

    @Test
    fun `ConversionResult getFormattedAirFryerTemp includes unit symbol`() {
        val fahrenheitResult = conversionResult(
            airFryerTemp = 350,
            unit = TemperatureUnit.FAHRENHEIT
        )
        val celsiusResult = conversionResult(
            airFryerTemp = 175,
            unit = TemperatureUnit.CELSIUS
        )

        assertThat(fahrenheitResult.getFormattedAirFryerTemp()).isEqualTo("350°F")
        assertThat(celsiusResult.getFormattedAirFryerTemp()).isEqualTo("175°C")
    }

    @Test
    fun `ConversionResult getFormattedOriginalTemp includes unit symbol`() {
        val fahrenheitResult = conversionResult(
            originalTemp = 375,
            unit = TemperatureUnit.FAHRENHEIT
        )
        val celsiusResult = conversionResult(
            originalTemp = 190,
            unit = TemperatureUnit.CELSIUS
        )

        assertThat(fahrenheitResult.getFormattedOriginalTemp()).isEqualTo("375°F")
        assertThat(celsiusResult.getFormattedOriginalTemp()).isEqualTo("190°C")
    }

    @Test
    fun `ConversionResult getTimeSavings calculates correctly`() {
        val result = conversionResult(
            originalTime = 30,
            airFryerTime = 24
        )

        assertThat(result.getTimeSavings()).isEqualTo(6) // 30 - 24
    }

    @Test
    fun `ConversionResult getTimeSavings handles zero savings`() {
        val result = conversionResult(
            originalTime = 25,
            airFryerTime = 25
        )

        assertThat(result.getTimeSavings()).isEqualTo(0)
    }

    @Test
    fun `ConversionResult getTimeReductionPercentage calculates correctly`() {
        val result = conversionResult(
            originalTime = 30,
            airFryerTime = 24
        )

        // (30 - 24) / 30 * 100 = 20%
        assertThat(result.getTimeReductionPercentage()).isEqualTo(20)
    }

    @Test
    fun `ConversionResult getTimeReductionPercentage handles various percentages`() {
        val result50Percent = conversionResult(originalTime = 40, airFryerTime = 20)
        val result25Percent = conversionResult(originalTime = 40, airFryerTime = 30)
        val result75Percent = conversionResult(originalTime = 40, airFryerTime = 10)

        assertThat(result50Percent.getTimeReductionPercentage()).isEqualTo(50)
        assertThat(result25Percent.getTimeReductionPercentage()).isEqualTo(25)
        assertThat(result75Percent.getTimeReductionPercentage()).isEqualTo(75)
    }

    @Test
    fun `ConversionResult getTimeReductionPercentage handles zero reduction`() {
        val result = conversionResult(
            originalTime = 25,
            airFryerTime = 25
        )

        assertThat(result.getTimeReductionPercentage()).isEqualTo(0)
    }

    @Test
    fun `ConversionValidation Valid isValid returns true`() {
        val validation = ConversionValidation.Valid

        assertThat(validation.isValid()).isTrue()
    }

    @Test
    fun `ConversionValidation Invalid isValid returns false`() {
        val validation = ConversionValidation.Invalid(
            listOf(ValidationError.TemperatureTooLow)
        )

        assertThat(validation.isValid()).isFalse()
    }

    @Test
    fun `ConversionValidation Invalid contains error list`() {
        val errors = listOf(
            ValidationError.TemperatureTooLow,
            ValidationError.TimeTooShort
        )
        val validation = ConversionValidation.Invalid(errors)

        assertThat(validation.errors).isEqualTo(errors)
        assertThat(validation.errors).hasSize(2)
    }

    @Test
    fun `ValidationError messages are descriptive`() {
        assertThat(ValidationError.TemperatureTooLow.message)
            .isEqualTo("Temperature is too low for safe cooking")
        assertThat(ValidationError.TemperatureTooHigh.message)
            .isEqualTo("Temperature is too high for this appliance")
        assertThat(ValidationError.TimeTooShort.message)
            .isEqualTo("Cooking time is too short")
        assertThat(ValidationError.TimeTooLong.message)
            .isEqualTo("Cooking time is unusually long")
    }

    @Test
    fun `ValidationError Custom accepts custom message`() {
        val customMessage = "Custom validation error message"
        val customError = ValidationError.Custom(customMessage)

        assertThat(customError.message).isEqualTo(customMessage)
    }

    @Test
    fun `ConversionInput boundary values work correctly`() {
        // Test minimum valid values
        val minFahrenheit = ConversionInput(200, 1, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        val minCelsius = ConversionInput(93, 1, FoodCategory.FROZEN_FOODS, TemperatureUnit.CELSIUS)

        assertThat(minFahrenheit.ovenTemperature).isEqualTo(200)
        assertThat(minCelsius.ovenTemperature).isEqualTo(93)

        // Test maximum valid values
        val maxFahrenheit = ConversionInput(500, 300, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        val maxCelsius = ConversionInput(260, 300, FoodCategory.FROZEN_FOODS, TemperatureUnit.CELSIUS)

        assertThat(maxFahrenheit.ovenTemperature).isEqualTo(500)
        assertThat(maxCelsius.ovenTemperature).isEqualTo(260)
    }
}