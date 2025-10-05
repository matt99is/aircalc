package com.aircalc.converter

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

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
