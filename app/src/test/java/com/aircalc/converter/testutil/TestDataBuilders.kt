package com.aircalc.converter.testutil

import com.aircalc.converter.domain.model.*

/**
 * Test data builders for creating test objects with sensible defaults.
 */

/**
 * Builder for ConversionInput test data.
 */
class ConversionInputBuilder {
    private var temperature = 375
    private var time = 25
    private var category = FoodCategory.FROZEN_FOODS
    private var unit = TemperatureUnit.FAHRENHEIT

    fun withTemperature(temp: Int) = apply { temperature = temp }
    fun withTime(time: Int) = apply { this.time = time }
    fun withCategory(cat: FoodCategory) = apply { category = cat }
    fun withUnit(unit: TemperatureUnit) = apply { this.unit = unit }

    fun build() = ConversionInput(temperature, time, category, unit)
}

/**
 * Builder for ConversionResult test data.
 */
class ConversionResultBuilder {
    private var originalTemperature = 375
    private var originalTime = 25
    private var airFryerTemperature = 350
    private var airFryerTime = 20
    private var temperatureUnit = TemperatureUnit.FAHRENHEIT
    private var foodCategory = FoodCategory.FROZEN_FOODS
    private var cookingTip = "Test cooking tip"
    private var temperatureReduction = 25
    private var timeReduction = 5

    fun withOriginalTemperature(temp: Int) = apply { originalTemperature = temp }
    fun withOriginalTime(time: Int) = apply { originalTime = time }
    fun withAirFryerTemperature(temp: Int) = apply { airFryerTemperature = temp }
    fun withAirFryerTime(time: Int) = apply { airFryerTime = time }
    fun withTemperatureUnit(unit: TemperatureUnit) = apply { temperatureUnit = unit }
    fun withFoodCategory(category: FoodCategory) = apply { foodCategory = category }
    fun withCookingTip(tip: String) = apply { cookingTip = tip }
    fun withTemperatureReduction(reduction: Int) = apply { temperatureReduction = reduction }
    fun withTimeReduction(reduction: Int) = apply { timeReduction = reduction }

    fun build() = ConversionResult(
        originalTemperature = originalTemperature,
        originalTime = originalTime,
        airFryerTemperature = airFryerTemperature,
        airFryerTimeMinutes = airFryerTime,
        temperatureUnit = temperatureUnit,
        foodCategory = foodCategory,
        cookingTip = cookingTip,
        temperatureReduction = temperatureReduction,
        timeReduction = timeReduction
    )
}

/**
 * Predefined test data for common scenarios.
 */
object TestData {

    // Common food categories
    val frozenFoods = FoodCategory.FROZEN_FOODS
    val freshVegetables = FoodCategory.FRESH_VEGETABLES
    val rawMeats = FoodCategory.RAW_MEATS
    val readyMeals = FoodCategory.READY_MEALS

    // Common temperature/time combinations
    object Common {
        val input375F25min = ConversionInputBuilder()
            .withTemperature(375)
            .withTime(25)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val input200C30min = ConversionInputBuilder()
            .withTemperature(200)
            .withTime(30)
            .withCategory(freshVegetables)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val meatInput400F45min = ConversionInputBuilder()
            .withTemperature(400)
            .withTime(45)
            .withCategory(rawMeats)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()
    }

    // Edge cases
    object EdgeCases {
        val minValidFahrenheit = ConversionInputBuilder()
            .withTemperature(200)
            .withTime(1)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val maxValidFahrenheit = ConversionInputBuilder()
            .withTemperature(500)
            .withTime(300)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val minValidCelsius = ConversionInputBuilder()
            .withTemperature(93)
            .withTime(1)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val maxValidCelsius = ConversionInputBuilder()
            .withTemperature(260)
            .withTime(300)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()
    }

    // Invalid cases for testing validation
    object Invalid {
        val temperatureTooLowF = ConversionInputBuilder()
            .withTemperature(199)
            .withTime(25)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val temperatureTooHighF = ConversionInputBuilder()
            .withTemperature(501)
            .withTime(25)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val temperatureTooLowC = ConversionInputBuilder()
            .withTemperature(92)
            .withTime(25)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val temperatureTooHighC = ConversionInputBuilder()
            .withTemperature(261)
            .withTime(25)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()

        val timeTooShort = ConversionInputBuilder()
            .withTemperature(375)
            .withTime(0)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()

        val timeTooLong = ConversionInputBuilder()
            .withTemperature(375)
            .withTime(301)
            .withCategory(frozenFoods)
            .withUnit(TemperatureUnit.FAHRENHEIT)
            .build()
    }
}

/**
 * Extension functions for creating test data more concisely.
 */
fun conversionInput(
    temperature: Int = 375,
    time: Int = 25,
    category: FoodCategory = FoodCategory.FROZEN_FOODS,
    unit: TemperatureUnit = TemperatureUnit.FAHRENHEIT
) = ConversionInput(temperature, time, category, unit)

fun conversionResult(
    originalTemp: Int = 375,
    originalTime: Int = 25,
    airFryerTemp: Int = 350,
    airFryerTime: Int = 20,
    unit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    category: FoodCategory = FoodCategory.FROZEN_FOODS
) = ConversionResult(
    originalTemperature = originalTemp,
    originalTime = originalTime,
    airFryerTemperature = airFryerTemp,
    airFryerTimeMinutes = airFryerTime,
    temperatureUnit = unit,
    foodCategory = category,
    cookingTip = category.cookingTip,
    temperatureReduction = originalTemp - airFryerTemp,
    timeReduction = originalTime - airFryerTime
)