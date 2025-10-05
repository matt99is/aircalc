package com.aircalc.converter.presentation.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages timer functionality with proper lifecycle handling.
 * Separated from ViewModel for reusability and testability.
 */
@Singleton
class TimerManager @Inject constructor() {

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Start timer with specified duration in minutes.
     */
    fun startTimer(minutes: Int) {
        stopTimer() // Stop any existing timer

        val totalSeconds = minutes * 60
        _timerState.value = TimerState(
            isStarted = true,
            isRunning = true,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            formattedTime = formatTime(totalSeconds)
        )

        timerJob = timerScope.launch {
            var remaining = totalSeconds

            while (remaining > 0 && _timerState.value.isRunning) {
                delay(1000) // Wait 1 second

                if (_timerState.value.isRunning) {
                    remaining--
                    val mins = remaining / 60
                    @Suppress("UNUSED_VARIABLE")
                    val secs = remaining % 60

                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = remaining,
                        remainingMinutes = mins,
                        formattedTime = formatTime(remaining)
                    )
                }
            }

            // Timer finished
            if (remaining <= 0) {
                _timerState.value = _timerState.value.copy(
                    isFinished = true,
                    isRunning = false,
                    formattedTime = "00:00"
                )
            }
        }
    }

    /**
     * Pause the timer.
     */
    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    /**
     * Resume the timer.
     */
    fun resumeTimer() {
        if (_timerState.value.isStarted && !_timerState.value.isFinished) {
            _timerState.value = _timerState.value.copy(isRunning = true)
        }
    }

    /**
     * Stop and reset the timer.
     */
    fun resetTimer() {
        stopTimer()
        _timerState.value = TimerState()
    }

    /**
     * Stop the timer job.
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Format seconds into MM:SS format.
     */
    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    /**
     * Cleanup resources.
     */
    fun cleanup() {
        stopTimer()
        timerScope.cancel()
    }
}

/**
 * Represents the state of the timer.
 */
data class TimerState(
    val isStarted: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val remainingMinutes: Int = 0,
    val formattedTime: String = "00:00"
) {
    /**
     * Get progress as a percentage (0.0 to 1.0).
     */
    fun getProgress(): Float {
        return if (totalSeconds > 0) {
            (totalSeconds - remainingSeconds).toFloat() / totalSeconds
        } else {
            0f
        }
    }

    /**
     * Check if timer should show announcement intervals.
     */
    fun shouldAnnounce(): Boolean {
        return isRunning && (
            remainingMinutes == 1 ||
            remainingMinutes in 2..5 ||
            remainingMinutes % 5 == 0
        )
    }
}