package com.aircalc.converter.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Unit tests for TemperatureUnit domain model.
 * Tests pure conversion logic and validation rules.
 */
class TemperatureUnitTest {

    @Test
    fun `convertTo returns same temperature when units are identical`() {
        val temperature = 375

        val fahrenheitResult = TemperatureUnit.FAHRENHEIT.convertTo(temperature, TemperatureUnit.FAHRENHEIT)
        val celsiusResult = TemperatureUnit.CELSIUS.convertTo(temperature, TemperatureUnit.CELSIUS)

        assertThat(fahrenheitResult).isEqualTo(temperature)
        assertThat(celsiusResult).isEqualTo(temperature)
    }

    @Test
    fun `convertTo handles Fahrenheit to Celsius correctly`() {
        // Test common cooking temperatures
        assertThat(TemperatureUnit.FAHRENHEIT.convertTo(32, TemperatureUnit.CELSIUS)).isEqualTo(0)
        assertThat(TemperatureUnit.FAHRENHEIT.convertTo(212, TemperatureUnit.CELSIUS)).isEqualTo(100)
        assertThat(TemperatureUnit.FAHRENHEIT.convertTo(350, TemperatureUnit.CELSIUS)).isEqualTo(176)
        assertThat(TemperatureUnit.FAHRENHEIT.convertTo(400, TemperatureUnit.CELSIUS)).isEqualTo(204)
        assertThat(TemperatureUnit.FAHRENHEIT.convertTo(450, TemperatureUnit.CELSIUS)).isEqualTo(232)
    }

    @Test
    fun `convertTo handles Celsius to Fahrenheit correctly`() {
        // Test common cooking temperatures
        assertThat(TemperatureUnit.CELSIUS.convertTo(0, TemperatureUnit.FAHRENHEIT)).isEqualTo(32)
        assertThat(TemperatureUnit.CELSIUS.convertTo(100, TemperatureUnit.FAHRENHEIT)).isEqualTo(212)
        assertThat(TemperatureUnit.CELSIUS.convertTo(175, TemperatureUnit.FAHRENHEIT)).isEqualTo(347)
        assertThat(TemperatureUnit.CELSIUS.convertTo(200, TemperatureUnit.FAHRENHEIT)).isEqualTo(392)
        assertThat(TemperatureUnit.CELSIUS.convertTo(230, TemperatureUnit.FAHRENHEIT)).isEqualTo(446)
    }

    @Test
    fun `convertTempReduction returns same value for Fahrenheit`() {
        val reduction = 25
        val result = TemperatureUnit.FAHRENHEIT.convertTempReduction(reduction)

        assertThat(result).isEqualTo(reduction)
    }

    @Test
    fun `convertTempReduction converts correctly for Celsius`() {
        // Test common reductions
        assertThat(TemperatureUnit.CELSIUS.convertTempReduction(18)).isEqualTo(10) // 18/1.8 = 10
        assertThat(TemperatureUnit.CELSIUS.convertTempReduction(25)).isEqualTo(13) // 25/1.8 = 13.89 -> 13
        assertThat(TemperatureUnit.CELSIUS.convertTempReduction(30)).isEqualTo(16) // 30/1.8 = 16.67 -> 16
    }

    @Test
    fun `isValidTemperature validates Fahrenheit range correctly`() {
        val unit = TemperatureUnit.FAHRENHEIT

        // Valid range: 200-500°F
        assertThat(unit.isValidTemperature(199)).isFalse()  // Below minimum
        assertThat(unit.isValidTemperature(200)).isTrue()   // At minimum
        assertThat(unit.isValidTemperature(350)).isTrue()   // In range
        assertThat(unit.isValidTemperature(500)).isTrue()   // At maximum
        assertThat(unit.isValidTemperature(501)).isFalse()  // Above maximum
    }

    @Test
    fun `isValidTemperature validates Celsius range correctly`() {
        val unit = TemperatureUnit.CELSIUS

        // Valid range: 93-260°C
        assertThat(unit.isValidTemperature(92)).isFalse()   // Below minimum
        assertThat(unit.isValidTemperature(93)).isTrue()    // At minimum
        assertThat(unit.isValidTemperature(175)).isTrue()   // In range
        assertThat(unit.isValidTemperature(260)).isTrue()   // At maximum
        assertThat(unit.isValidTemperature(261)).isFalse()  // Above maximum
    }

    @Test
    fun `getDefault returns Fahrenheit`() {
        assertThat(TemperatureUnit.getDefault()).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }

    @Test
    fun `enum properties are correct`() {
        assertThat(TemperatureUnit.FAHRENHEIT.symbol).isEqualTo("°F")
        assertThat(TemperatureUnit.FAHRENHEIT.displayName).isEqualTo("Fahrenheit")

        assertThat(TemperatureUnit.CELSIUS.symbol).isEqualTo("°C")
        assertThat(TemperatureUnit.CELSIUS.displayName).isEqualTo("Celsius")
    }
}

/**
 * Parameterized tests for boundary conditions.
 */
@RunWith(Parameterized::class)
class TemperatureUnitBoundaryTest(
    private val temperature: Int,
    private val unit: TemperatureUnit,
    private val expected: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}{1} should be valid: {2}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // Fahrenheit boundaries
                arrayOf(199, TemperatureUnit.FAHRENHEIT, false),
                arrayOf(200, TemperatureUnit.FAHRENHEIT, true),
                arrayOf(350, TemperatureUnit.FAHRENHEIT, true),
                arrayOf(500, TemperatureUnit.FAHRENHEIT, true),
                arrayOf(501, TemperatureUnit.FAHRENHEIT, false),

                // Celsius boundaries
                arrayOf(92, TemperatureUnit.CELSIUS, false),
                arrayOf(93, TemperatureUnit.CELSIUS, true),
                arrayOf(175, TemperatureUnit.CELSIUS, true),
                arrayOf(260, TemperatureUnit.CELSIUS, true),
                arrayOf(261, TemperatureUnit.CELSIUS, false)
            )
        }
    }

    @Test
    fun `isValidTemperature handles boundary conditions correctly`() {
        assertThat(unit.isValidTemperature(temperature)).isEqualTo(expected)
    }
}

/**
 * Parameterized tests for temperature conversions.
 */
@RunWith(Parameterized::class)
class TemperatureConversionTest(
    private val inputTemp: Int,
    private val fromUnit: TemperatureUnit,
    private val toUnit: TemperatureUnit,
    private val expectedTemp: Int
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}{1} -> {2}{3} = {4}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // Fahrenheit to Celsius
                arrayOf(32, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 0),
                arrayOf(212, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 100),
                arrayOf(350, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 176),
                arrayOf(375, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 190),
                arrayOf(400, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 204),
                arrayOf(450, TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS, 232),

                // Celsius to Fahrenheit
                arrayOf(0, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 32),
                arrayOf(100, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 212),
                arrayOf(175, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 347),
                arrayOf(190, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 374),
                arrayOf(200, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 392),
                arrayOf(230, TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT, 446)
            )
        }
    }

    @Test
    fun `temperature conversion is accurate`() {
        val result = fromUnit.convertTo(inputTemp, toUnit)
        assertThat(result).isEqualTo(expectedTemp)
    }
}