package com.aircalc.converter

enum class FoodCategory(
    val displayName: String,
    val icon: String,
    val tempReduction: Int, // Fahrenheit reduction
    val timeMultiplier: Double,
    val tip: String,
    val description: String
) {
    FROZEN_FOODS(
        "Frozen Foods",
        "🧊",
        25,
        0.8,
        "Shake basket halfway through cooking",
        "Frozen fries, nuggets, vegetables"
    ),
    FRESH_VEGETABLES(
        "Fresh Veg",
        "🥕",
        30,
        0.75,
        "Toss vegetables halfway through for even cooking",
        "Broccoli, carrots, Brussels sprouts"
    ),
    MEATS_RAW(
        "Raw Meats",
        "🥩",
        25,
        0.85,
        "Check internal temperature before serving",
        "Chicken, beef, pork, fish"
    ),
    BAKED_GOODS(
        "Baked Goods",
        "🧁",
        25,
        0.7,
        "Use parchment paper and check frequently",
        "Cookies, muffins, pastries"
    ),
    REFRIGERATED_READY_MEALS(
        "Ready Meals",
        "🍱",
        25,
        0.75,
        "Pierce any sealed packaging before cooking",
        "Pre-made meals, leftovers"
    )
}

enum class TemperatureUnit(val symbol: String) {
    FAHRENHEIT("°F"),
    CELSIUS("°C")
}

data class ConversionInput(
    val ovenTemp: Int,
    val cookingTimeMinutes: Int,
    val foodCategory: FoodCategory,
    val temperatureUnit: TemperatureUnit
)

data class ConversionResult(
    val airFryerTemp: Int,
    val airFryerTimeMinutes: Int,
    val tip: String,
    val temperatureUnit: TemperatureUnit
)

object AirFryerConverter {
    fun convertToAirFryer(input: ConversionInput): ConversionResult {
        val tempReduction = if (input.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            input.foodCategory.tempReduction
        } else {
            // Convert Fahrenheit reduction to Celsius (F° reduction ÷ 1.8)
            (input.foodCategory.tempReduction / 1.8).toInt()
        }

        val airFryerTemp = input.ovenTemp - tempReduction
        val airFryerTime = (input.cookingTimeMinutes * input.foodCategory.timeMultiplier).toInt()

        return ConversionResult(
            airFryerTemp = airFryerTemp,
            airFryerTimeMinutes = airFryerTime,
            tip = input.foodCategory.tip,
            temperatureUnit = input.temperatureUnit
        )
    }
}