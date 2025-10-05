package com.aircalc.converter.domain.model

/**
 * Domain model representing different food categories with their conversion properties.
 * This is part of the domain layer and contains no UI or framework dependencies.
 */
data class FoodCategory(
    val id: String,
    val displayName: String,
    val icon: String,
    val tempReductionFahrenheit: Int,
    val timeMultiplier: Double,
    val cookingTip: String,
    val description: String
) {
    companion object {
        val FROZEN_FOODS = FoodCategory(
            id = "frozen_foods",
            displayName = "Frozen Foods",
            icon = "🧊",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.8,
            cookingTip = "Shake basket halfway through cooking",
            description = "Frozen fries, nuggets, vegetables"
        )

        val FRESH_VEGETABLES = FoodCategory(
            id = "fresh_vegetables",
            displayName = "Fresh Veg",
            icon = "🥕",
            tempReductionFahrenheit = 30,
            timeMultiplier = 0.75,
            cookingTip = "Toss vegetables halfway through for even cooking",
            description = "Broccoli, carrots, Brussels sprouts"
        )

        val RAW_MEATS = FoodCategory(
            id = "raw_meats",
            displayName = "Raw Meats",
            icon = "🥩",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.85,
            cookingTip = "Check internal temperature before serving",
            description = "Chicken, beef, pork, fish"
        )

        val BAKED_GOODS = FoodCategory(
            id = "baked_goods",
            displayName = "Baked Goods",
            icon = "🧁",
            tempReductionFahrenheit = 25,
            timeMultiplier = 0.7,
            cookingTip = "Use parchment paper and check frequently",
            description = "Cookies, muffins, pastries"
        )

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
            BAKED_GOODS,
            READY_MEALS
        )

        fun getById(id: String): FoodCategory? = getAllCategories().find { it.id == id }
    }
}