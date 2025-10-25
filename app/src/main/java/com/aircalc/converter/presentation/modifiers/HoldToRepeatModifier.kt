package com.aircalc.converter.presentation.modifiers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Modifier that enables hold-to-repeat functionality with 3-phase acceleration.
 * - Tap: Single increment
 * - Hold: Continuous increments with acceleration (slow → medium → fast)
 * - Haptic feedback pulses on each increment
 */
@Composable
fun Modifier.holdToRepeat(
    onIncrement: () -> Unit,
    hapticFeedback: HapticFeedback
): Modifier {
    val scope = rememberCoroutineScope()
    // Remember the latest callbacks to avoid stale closures
    val currentOnIncrement by rememberUpdatedState(onIncrement)
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)

    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)

            val repeatJob = scope.launch(Dispatchers.Default) {
                // Initial increment
                withContext(Dispatchers.Main) {
                    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnIncrement()
                }

                delay(300) // Initial delay

                // Phase 1: Slow (8 increments at 200ms intervals)
                repeat(8) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(200)
                }

                // Phase 2: Medium (10 increments at 150ms intervals)
                repeat(10) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(150)
                }

                // Phase 3: Fast (continuous at 80ms intervals)
                while (true) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(80)
                }
            }

            // Wait for release
            do {
                val event = awaitPointerEvent()
            } while (event.changes.any { it.pressed })

            repeatJob.cancel()
        }
    }
}
