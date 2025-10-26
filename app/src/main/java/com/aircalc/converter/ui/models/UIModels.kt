package com.aircalc.converter

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * UI-specific food category enum for MainActivity backward compatibility.
 * Maps to domain model FoodCategory.
 */
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
        0,  // Updated: no temp reduction for frozen foods
        0.5, // Updated: 50% time reduction
        "Shake basket halfway through cooking",
        "Frozen fries, nuggets, vegetables"
    ),
    FRESH_VEGETABLES(
        "Fresh Veg",
        "🥕",
        25,
        0.8,
        "Toss vegetables halfway through for even cooking",
        "Broccoli, carrots, Brussels sprouts"
    ),
    MEATS_RAW(
        "Raw Meats",
        "🥩",
        25,
        0.8,
        "Check internal temperature before serving",
        "Chicken, beef, pork, fish"
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

/**
 * UI-specific temperature unit enum for MainActivity backward compatibility.
 */
enum class TemperatureUnit(val symbol: String) {
    FAHRENHEIT("°F"),
    CELSIUS("°C")
}

/**
 * Conversion result for legacy UI usage.
 * Note: Prefer using domain.model.ConversionResult when possible.
 */
data class ConversionResult(
    val airFryerTemp: Int,
    val airFryerTimeMinutes: Int,
    val tip: String,
    val temperatureUnit: TemperatureUnit
)

/**
 * Timer state interface for ConversionResultsScreen.
 * Allows using either legacy timer or ViewModel-based timer.
 */
abstract class TimerState {
    abstract val timeLeftSeconds: Int
    abstract val isRunning: Boolean
    abstract val isFinished: Boolean
    abstract val timeLeftFormatted: String

    abstract fun startTimer()
    abstract fun pauseTimer()
    abstract fun resetTimer(minutes: Int)
}
