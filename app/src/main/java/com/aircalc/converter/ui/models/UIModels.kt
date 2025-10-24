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
 * Timer state for legacy Compose-based timer.
 * Note: This is different from presentation.timer.TimerState.
 */
@Composable
fun rememberTimerState(initialTimeMinutes: Int = 0): TimerState {
    return remember { TimerState(initialTimeMinutes) }
}

class TimerState(initialTimeMinutes: Int) {
    private var _timeLeftSeconds by mutableStateOf(initialTimeMinutes * 60)
    private var _isRunning by mutableStateOf(false)
    private var _isFinished by mutableStateOf(false)

    // Callback for when timer finishes
    var onTimerFinished: (() -> Unit)? = null

    val timeLeftSeconds: Int get() = _timeLeftSeconds
    val isRunning: Boolean get() = _isRunning
    val isFinished: Boolean get() = _isFinished

    val timeLeftFormatted: String
        get() {
            val minutes = _timeLeftSeconds / 60
            val seconds = _timeLeftSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    fun startTimer() {
        if (_timeLeftSeconds > 0 && !_isRunning) {
            _isRunning = true
            _isFinished = false
        }
    }

    fun pauseTimer() {
        _isRunning = false
    }

    fun resetTimer(newTimeMinutes: Int) {
        _isRunning = false
        _isFinished = false
        _timeLeftSeconds = newTimeMinutes * 60
    }

    fun tick() {
        if (_isRunning && _timeLeftSeconds > 0) {
            _timeLeftSeconds--
            if (_timeLeftSeconds == 0) {
                _isRunning = false
                _isFinished = true
                onTimerFinished?.invoke()
            }
        }
    }
}

@Composable
fun LaunchedTimer(timerState: TimerState) {
    LaunchedEffect(timerState.isRunning) {
        while (timerState.isRunning) {
            delay(1000)
            timerState.tick()
        }
    }
}
