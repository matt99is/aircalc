package com.aircalc.converter.data.datasource

import com.aircalc.converter.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for conversion operations.
 * Contains the actual conversion algorithm and data access logic.
 */
@Singleton
class ConversionDataSource @Inject constructor() {

    private val conversionHistory = mutableListOf<ConversionResult>()

    /**
     * Perform the actual air fryer conversion calculation.
     */
    fun performConversion(input: ConversionInput): ConversionResult {
        val tempReduction = input.temperatureUnit.convertTempReduction(
            input.foodCategory.tempReductionFahrenheit
        )

        val airFryerTemp = input.ovenTemperature - tempReduction
        val airFryerTime = (input.cookingTimeMinutes * input.foodCategory.timeMultiplier).toInt()

        return ConversionResult(
            originalTemperature = input.ovenTemperature,
            originalTime = input.cookingTimeMinutes,
            airFryerTemperature = airFryerTemp,
            airFryerTimeMinutes = airFryerTime,
            temperatureUnit = input.temperatureUnit,
            foodCategory = input.foodCategory,
            cookingTip = input.foodCategory.cookingTip,
            temperatureReduction = tempReduction,
            timeReduction = input.cookingTimeMinutes - airFryerTime
        )
    }

    /**
     * Get all available food categories.
     */
    fun getFoodCategories(): List<FoodCategory> {
        return FoodCategory.getAllCategories()
    }

    /**
     * Get a specific food category by ID.
     */
    fun getFoodCategory(id: String): FoodCategory? {
        return FoodCategory.getById(id)
    }

    /**
     * Save conversion result to history.
     */
    fun saveToHistory(result: ConversionResult) {
        conversionHistory.add(0, result) // Add to beginning

        // Keep only last 20 conversions
        if (conversionHistory.size > 20) {
            conversionHistory.removeAt(conversionHistory.size - 1)
        }
    }

    /**
     * Get conversion history.
     */
    fun getHistory(): List<ConversionResult> {
        return conversionHistory.toList()
    }

    /**
     * Clear conversion history.
     */
    fun clearHistory() {
        conversionHistory.clear()
    }
}