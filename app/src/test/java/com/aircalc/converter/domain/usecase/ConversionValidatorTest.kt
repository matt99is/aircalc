package com.aircalc.converter.domain.usecase

import com.aircalc.converter.domain.model.*
import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConversionValidator.
 * Tests all business rules and validation logic.
 */
class ConversionValidatorTest {

    private lateinit var validator: ConversionValidator

    @Before
    fun setUp() {
        validator = ConversionValidator()
    }

    // MARK: - Basic Validation Tests

    @Test
    fun `validateInput returns Valid for correct input`() {
        val input = TestData.Common.input375F25min

        val result = validator.validateInput(input)

        assertThat(result).isEqualTo(ConversionValidation.Valid)
        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput returns Invalid for multiple errors`() {
        val input = ConversionInputBuilder()
            .withTemperature(199) // Too low
            .withTime(0)          // Too short
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        assertThat(result).isInstanceOf(ConversionValidation.Invalid::class.java)
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).hasSize(2)
    }

    // MARK: - Temperature Validation Tests

    @Test
    fun `validateInput rejects temperature too low for Fahrenheit`() {
        val input = ConversionInputBuilder()
            .withTemperature(199)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TemperatureTooLow)
    }

    @Test
    fun `validateInput rejects temperature too high for Fahrenheit`() {
        val input = ConversionInputBuilder()
            .withTemperature(501)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TemperatureTooHigh)
    }

    @Test
    fun `validateInput accepts valid Fahrenheit temperature range`() {
        val validTemperatures = listOf(200, 350, 400, 500)

        validTemperatures.forEach { temp ->
            val input = ConversionInputBuilder()
                .withTemperature(temp)
                .withUnit(TemperatureUnit.FAHRENHEIT)
                .build()

            val result = validator.validateInput(input)
            assertThat(result.isValid()).isTrue()
        }
    }

    @Test
    fun `validateInput rejects temperature too low for Celsius`() {
        val input = ConversionInputBuilder()
            .withTemperature(92)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TemperatureTooLow)
    }

    @Test
    fun `validateInput rejects temperature too high for Celsius`() {
        val input = ConversionInputBuilder()
            .withTemperature(261)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TemperatureTooHigh)
    }

    @Test
    fun `validateInput accepts valid Celsius temperature range`() {
        val validTemperatures = listOf(93, 175, 200, 260)

        validTemperatures.forEach { temp ->
            val input = ConversionInputBuilder()
                .withTemperature(temp)
                .withUnit(TemperatureUnit.CELSIUS)
                .build()

            val result = validator.validateInput(input)
            assertThat(result.isValid()).isTrue()
        }
    }

    // MARK: - Time Validation Tests

    @Test
    fun `validateInput rejects cooking time too short`() {
        val input = ConversionInputBuilder()
            .withTime(0)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TimeTooShort)
    }

    @Test
    fun `validateInput rejects cooking time too long`() {
        val input = ConversionInputBuilder()
            .withTime(301) // Over 5 hours
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        assertThat(invalid.errors).contains(ValidationError.TimeTooLong)
    }

    @Test
    fun `validateInput accepts valid cooking time range`() {
        val validTimes = listOf(1, 15, 60, 120, 300)

        validTimes.forEach { time ->
            val input = ConversionInputBuilder()
                .withTime(time)
                .build()

            val result = validator.validateInput(input)
            assertThat(result.isValid()).isTrue()
        }
    }

    // MARK: - Food Category Compatibility Tests

    @Test
    fun `validateInput accepts valid temperature for ready meals`() {
        val input = ConversionInputBuilder()
            .withTemperature(401)
            .withCategory(FoodCategory.READY_MEALS)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput accepts another valid temperature for ready meals`() {
        val input = ConversionInputBuilder()
            .withTemperature(350)
            .withCategory(FoodCategory.READY_MEALS)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput rejects temperature too low for vegetables`() {
        val input = ConversionInputBuilder()
            .withTemperature(299) // Too low for vegetables
            .withCategory(FoodCategory.FRESH_VEGETABLES)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        val customError = invalid.errors[0] as ValidationError.Custom
        assertThat(customError.message).contains("Temperature too low for vegetables")
    }

    @Test
    fun `validateInput accepts valid temperature for vegetables`() {
        val input = ConversionInputBuilder()
            .withTemperature(375) // Valid for vegetables
            .withCategory(FoodCategory.FRESH_VEGETABLES)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput rejects temperature too low for raw meat`() {
        val input = ConversionInputBuilder()
            .withTemperature(324) // Too low for raw meat
            .withCategory(FoodCategory.RAW_MEATS)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isFalse()
        val invalid = result as ConversionValidation.Invalid
        val customError = invalid.errors[0] as ValidationError.Custom
        assertThat(customError.message).contains("Temperature too low for raw meat")
        assertThat(customError.message).contains("food safety concern")
    }

    @Test
    fun `validateInput accepts valid temperature for raw meat`() {
        val input = ConversionInputBuilder()
            .withTemperature(375) // Valid for raw meat
            .withCategory(FoodCategory.RAW_MEATS)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput handles Celsius temperatures for food category compatibility`() {
        // Test ready meals with high Celsius temperature - should be valid
        // 205°C = ~401°F, valid for ready meals
        val input = ConversionInputBuilder()
            .withTemperature(205)
            .withCategory(FoodCategory.READY_MEALS)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    @Test
    fun `validateInput allows other food categories without specific restrictions`() {
        val input = ConversionInputBuilder()
            .withTemperature(375)
            .withCategory(FoodCategory.FROZEN_FOODS)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val result = validator.validateInput(input)

        assertThat(result.isValid()).isTrue()
    }

    // MARK: - Result Validation Tests

    @Test
    fun `validateResult accepts valid conversion result`() {
        val result = conversionResult(
            airFryerTemp = 350,
            airFryerTime = 20,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val validation = validator.validateResult(result)

        assertThat(validation.isValid()).isTrue()
    }

    @Test
    fun `validateResult rejects air fryer temperature too low in Fahrenheit`() {
        val result = conversionResult(
            airFryerTemp = 199,
            airFryerTime = 20,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val validation = validator.validateResult(result)

        assertThat(validation.isValid()).isFalse()
        val invalid = validation as ConversionValidation.Invalid
        val customError = invalid.errors[0] as ValidationError.Custom
        assertThat(customError.message).contains("Resulting air fryer temperature is too low")
    }

    @Test
    fun `validateResult rejects air fryer temperature too low in Celsius`() {
        val result = conversionResult(
            airFryerTemp = 92,
            airFryerTime = 20,
            unit = TemperatureUnit.CELSIUS
        )

        val validation = validator.validateResult(result)

        assertThat(validation.isValid()).isFalse()
        val invalid = validation as ConversionValidation.Invalid
        val customError = invalid.errors[0] as ValidationError.Custom
        assertThat(customError.message).contains("Resulting air fryer temperature is too low")
    }

    @Test
    fun `validateResult rejects air fryer time too short`() {
        val result = conversionResult(
            airFryerTemp = 350,
            airFryerTime = 0,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val validation = validator.validateResult(result)

        assertThat(validation.isValid()).isFalse()
        val invalid = validation as ConversionValidation.Invalid
        val customError = invalid.errors[0] as ValidationError.Custom
        assertThat(customError.message).contains("Resulting cooking time is too short")
    }

    @Test
    fun `validateResult accepts boundary valid values`() {
        val fahrenheitResult = conversionResult(
            airFryerTemp = 200, // Minimum valid Fahrenheit
            airFryerTime = 1,   // Minimum valid time
            unit = TemperatureUnit.FAHRENHEIT
        )

        val celsiusResult = conversionResult(
            airFryerTemp = 93,  // Minimum valid Celsius
            airFryerTime = 1,   // Minimum valid time
            unit = TemperatureUnit.CELSIUS
        )

        assertThat(validator.validateResult(fahrenheitResult).isValid()).isTrue()
        assertThat(validator.validateResult(celsiusResult).isValid()).isTrue()
    }
}