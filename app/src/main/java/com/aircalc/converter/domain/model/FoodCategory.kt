package com.aircalc.converter.domain.model

/**
 * Domain model representing different food categories with their conversion properties.
 * This is part of the domain layer and contains no UI or framework dependencies.
 *
 * Conversion Formulas (based on industry best practices and package instructions):
 *
 * FROZEN FOODS:
 * - Temperature: Use SAME temperature as oven (no reduction)
 * - Time: Multiply by 0.50 (50% reduction)
 * - Example: 400°F for 20 mins → 400°F for 10 mins
 *
 * FRESH VEGETABLES:
 * - Temperature: Reduce by 25°F (or 15°C)
 * - Time: Multiply by 0.80 (20% reduction)
 * - Example: 400°F for 30 mins → 375°F for 24 mins
 *
 * RAW MEATS:
 * - Temperature: Reduce by 25°F (or 15°C)
 * - Time: Multiply by 0.80 (20% reduction)
 * - Example: 375°F for 40 mins → 350°F for 32 mins
 *
 * READY MEALS:
 * - Temperature: Reduce by 25°F (or 15°C)
 * - Time: Multiply by 0.75 (25% reduction)
 * - Example: 400°F for 40 mins → 375°F for 30 mins
 */
data class FoodCategory(
    val id: String,
    val displayName: String,
    val icon: String,
    /** Temperature reduction in Fahrenheit. 0 means use same temperature as oven. */
    val tempReductionFahrenheit: Int,
    /** Time multiplier. 0.5 = 50% of original time, 0.75 = 75% of original time, etc. */
    val timeMultiplier: Double,
    val cookingTip: String,
    val description: String
) {
    companion object {
        /**
         * Frozen foods (fries, nuggets, frozen vegetables).
         * Uses SAME temperature but cuts cooking time in HALF.
         * This matches manufacturer package instructions and industry best practices.
         */
        val FROZEN_FOODS = FoodCategory(
            id = "frozen_foods",
            displayName = "Frozen Foods",
            icon = "🧊",
            tempReductionFahrenheit = 0,
            timeMultiplier = 0.5,
            cookingTip = "Shake basket halfway through cooking",
            description = "Frozen fries, nuggets, vegetables"
        )

        /**
         * Fresh vegetables (broccoli, carrots, Brussels sprouts).
         * Reduces temperature by 25°F and cooking time by 20%.
         */
        val FRESH_VEGETABLES = FoodCategory(
            id = "fresh_vegetables",
            displayName = "Fresh Veg",
            icon = "🥕",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.80,
            cookingTip = "Toss vegetables halfway through for even cooking",
            description = "Broccoli, carrots, Brussels sprouts"
        )

        /**
         * Raw meats (chicken, beef, pork, fish).
         * Reduces temperature by 25°F and cooking time by 20%.
         */
        val RAW_MEATS = FoodCategory(
            id = "raw_meats",
            displayName = "Raw Meats",
            icon = "🥩",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.80,
            cookingTip = "Check internal temperature before serving",
            description = "Chicken, beef, pork, fish"
        )

        /**
         * Ready meals and leftovers.
         * Reduces temperature by 25°F and cooking time by 25%.
         */
        val READY_MEALS = FoodCategory(
            id = "ready_meals",
            displayName = "Ready Meals",
            icon = "🍱",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.75,
            cookingTip = "Pierce any sealed packaging before cooking",
            description = "Pre-made meals, leftovers"
        )

        fun getAllCategories(): List<FoodCategory> = listOf(
            FROZEN_FOODS,
            FRESH_VEGETABLES,
            RAW_MEATS,
            READY_MEALS
        )

        fun getById(id: String): FoodCategory? = getAllCategories().find { it.id == id }
    }
}