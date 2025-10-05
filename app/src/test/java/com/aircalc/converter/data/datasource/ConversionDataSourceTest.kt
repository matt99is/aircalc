package com.aircalc.converter.data.datasource

import com.aircalc.converter.domain.model.*
import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConversionDataSource.
 * Tests the core conversion algorithm and data operations.
 */
class ConversionDataSourceTest {

    private lateinit var dataSource: ConversionDataSource

    @Before
    fun setUp() {
        dataSource = ConversionDataSource()
    }

    @Test
    fun `performConversion calculates correct air fryer settings for frozen foods`() {
        val input = conversionInput(
            temperature = 400,
            time = 30,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.originalTemperature).isEqualTo(400)
        assertThat(result.originalTime).isEqualTo(30)
        assertThat(result.airFryerTemperature).isEqualTo(375) // 400 - 25
        assertThat(result.airFryerTimeMinutes).isEqualTo(24)  // 30 * 0.8
        assertThat(result.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
        assertThat(result.foodCategory).isEqualTo(FoodCategory.FROZEN_FOODS)
        assertThat(result.temperatureReduction).isEqualTo(25)
        assertThat(result.timeReduction).isEqualTo(6) // 30 - 24
    }

    @Test
    fun `performConversion calculates correct air fryer settings for fresh vegetables`() {
        val input = conversionInput(
            temperature = 375,
            time = 40,
            category = FoodCategory.FRESH_VEGETABLES,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.airFryerTemperature).isEqualTo(345) // 375 - 30
        assertThat(result.airFryerTimeMinutes).isEqualTo(30)  // 40 * 0.75
        assertThat(result.temperatureReduction).isEqualTo(30)
        assertThat(result.timeReduction).isEqualTo(10) // 40 - 30
    }

    @Test
    fun `performConversion calculates correct air fryer settings for raw meats`() {
        val input = conversionInput(
            temperature = 425,
            time = 60,
            category = FoodCategory.RAW_MEATS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.airFryerTemperature).isEqualTo(400) // 425 - 25
        assertThat(result.airFryerTimeMinutes).isEqualTo(51)  // 60 * 0.85
        assertThat(result.temperatureReduction).isEqualTo(25)
        assertThat(result.timeReduction).isEqualTo(9) // 60 - 51
    }

    @Test
    fun `performConversion calculates correct air fryer settings for baked goods`() {
        val input = conversionInput(
            temperature = 350,
            time = 25,
            category = FoodCategory.BAKED_GOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.airFryerTemperature).isEqualTo(325) // 350 - 25
        assertThat(result.airFryerTimeMinutes).isEqualTo(17)  // 25 * 0.7
        assertThat(result.temperatureReduction).isEqualTo(25)
        assertThat(result.timeReduction).isEqualTo(8) // 25 - 17
    }

    @Test
    fun `performConversion handles Celsius input correctly`() {
        val input = conversionInput(
            temperature = 200,
            time = 30,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.CELSIUS
        )

        val result = dataSource.performConversion(input)

        // Temperature reduction: 25°F = ~14°C (25/1.8 = 13.89 -> 13)
        assertThat(result.airFryerTemperature).isEqualTo(187) // 200 - 13
        assertThat(result.airFryerTimeMinutes).isEqualTo(24)  // 30 * 0.8
        assertThat(result.temperatureUnit).isEqualTo(TemperatureUnit.CELSIUS)
        assertThat(result.temperatureReduction).isEqualTo(13)
    }

    @Test
    fun `performConversion preserves cooking tip from food category`() {
        val input = conversionInput(category = FoodCategory.FRESH_VEGETABLES)

        val result = dataSource.performConversion(input)

        assertThat(result.cookingTip).isEqualTo(FoodCategory.FRESH_VEGETABLES.cookingTip)
    }

    @Test
    fun `performConversion handles edge case minimum values`() {
        val input = conversionInput(
            temperature = 200,
            time = 1,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.airFryerTemperature).isEqualTo(175) // 200 - 25
        assertThat(result.airFryerTimeMinutes).isEqualTo(0)   // 1 * 0.8 = 0.8 -> 0
    }

    @Test
    fun `performConversion handles edge case maximum values`() {
        val input = conversionInput(
            temperature = 500,
            time = 300,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        val result = dataSource.performConversion(input)

        assertThat(result.airFryerTemperature).isEqualTo(475) // 500 - 25
        assertThat(result.airFryerTimeMinutes).isEqualTo(240) // 300 * 0.8
    }

    @Test
    fun `getFoodCategories returns all available categories`() {
        val categories = dataSource.getFoodCategories()

        assertThat(categories).hasSize(5)
        assertThat(categories).contains(FoodCategory.FROZEN_FOODS)
        assertThat(categories).contains(FoodCategory.FRESH_VEGETABLES)
        assertThat(categories).contains(FoodCategory.RAW_MEATS)
        assertThat(categories).contains(FoodCategory.BAKED_GOODS)
        assertThat(categories).contains(FoodCategory.READY_MEALS)
    }

    @Test
    fun `getFoodCategory returns correct category by id`() {
        val frozenFoods = dataSource.getFoodCategory("frozen_foods")
        val vegetables = dataSource.getFoodCategory("fresh_vegetables")
        val nonExistent = dataSource.getFoodCategory("non_existent")

        assertThat(frozenFoods).isEqualTo(FoodCategory.FROZEN_FOODS)
        assertThat(vegetables).isEqualTo(FoodCategory.FRESH_VEGETABLES)
        assertThat(nonExistent).isNull()
    }

    @Test
    fun `saveToHistory adds conversion to beginning of list`() {
        val result1 = conversionResult(originalTemp = 350)
        val result2 = conversionResult(originalTemp = 375)

        dataSource.saveToHistory(result1)
        dataSource.saveToHistory(result2)

        val history = dataSource.getHistory()
        assertThat(history).hasSize(2)
        assertThat(history[0]).isEqualTo(result2) // Most recent first
        assertThat(history[1]).isEqualTo(result1)
    }

    @Test
    fun `saveToHistory limits history to 20 items`() {
        // Add 25 items
        repeat(25) { index ->
            val result = conversionResult(originalTemp = 300 + index)
            dataSource.saveToHistory(result)
        }

        val history = dataSource.getHistory()
        assertThat(history).hasSize(20)
        // Should contain the last 20 items (324, 323, ..., 305)
        assertThat(history[0].originalTemperature).isEqualTo(324) // Most recent
        assertThat(history[19].originalTemperature).isEqualTo(305) // Oldest kept
    }

    @Test
    fun `clearHistory removes all items`() {
        dataSource.saveToHistory(conversionResult())
        dataSource.saveToHistory(conversionResult())

        assertThat(dataSource.getHistory()).hasSize(2)

        dataSource.clearHistory()

        assertThat(dataSource.getHistory()).isEmpty()
    }

    @Test
    fun `getHistory returns defensive copy`() {
        val result = conversionResult()
        dataSource.saveToHistory(result)

        val history1 = dataSource.getHistory()
        val history2 = dataSource.getHistory()

        assertThat(history1).isNotSameInstanceAs(history2)
        assertThat(history1).isEqualTo(history2)
    }
}