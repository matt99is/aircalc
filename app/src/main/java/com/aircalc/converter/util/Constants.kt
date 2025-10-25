package com.aircalc.converter.util

/**
 * Application-wide constants for configuration values.
 * Centralizes all magic numbers for easy maintenance and configuration.
 */
object Constants {

    /**
     * Timer-related constants
     */
    object Timer {
        /** Alarm sound playback duration in milliseconds */
        const val ALARM_SOUND_DURATION_MS = 5000L

        /** Wake lock buffer time added to timer duration in milliseconds */
        const val WAKE_LOCK_BUFFER_MS = 5 * 60 * 1000L // 5 minutes

        /** Notification update interval when app is in background in milliseconds */
        const val NOTIFICATION_UPDATE_INTERVAL_BACKGROUND_MS = 30000L // 30 seconds

        /** DataStore save debounce delay in milliseconds */
        const val DATASTORE_SAVE_DEBOUNCE_MS = 500L

        /** Delay for timer initialization in milliseconds */
        const val TIMER_INIT_DELAY_MS = 100L

        /** Delay after timer pause in milliseconds */
        const val TIMER_PAUSE_DELAY_MS = 50L

        /** Delay before navigation after reset in milliseconds */
        const val TIMER_RESET_DELAY_MS = 150L
    }

    /**
     * Temperature input constants
     */
    object Temperature {
        // Celsius
        const val CELSIUS_MIN = 120
        const val CELSIUS_MAX = 250
        const val CELSIUS_DEFAULT = 180
        const val CELSIUS_INCREMENT = 5

        // Fahrenheit
        const val FAHRENHEIT_MIN = 250
        const val FAHRENHEIT_MAX = 480
        const val FAHRENHEIT_DEFAULT = 350
        const val FAHRENHEIT_INCREMENT = 5
    }

    /**
     * Cooking time input constants
     */
    object Time {
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 180
        const val DEFAULT_MINUTES = 25
        const val INCREMENT_MINUTES = 1
    }

    /**
     * Hold-to-repeat interaction constants for increment/decrement buttons
     */
    object HoldToRepeat {
        /** Initial delay before starting auto-repeat in milliseconds */
        const val INITIAL_DELAY_MS = 300L

        /** Delay between increments during slow phase in milliseconds */
        const val SLOW_PHASE_DELAY_MS = 200L

        /** Number of increments in slow phase */
        const val SLOW_PHASE_COUNT = 8

        /** Delay between increments during medium phase in milliseconds */
        const val MEDIUM_PHASE_DELAY_MS = 150L

        /** Number of increments in medium phase */
        const val MEDIUM_PHASE_COUNT = 10

        /** Delay between increments during fast phase in milliseconds */
        const val FAST_PHASE_DELAY_MS = 80L
    }

    /**
     * Vibration pattern constants
     */
    object Vibration {
        /** Vibration pattern: delays and vibration durations in milliseconds */
        val PATTERN = longArrayOf(0, 500, 200, 500, 200, 500)

        /** Vibration amplitudes (for API 26+) */
        val AMPLITUDES = intArrayOf(0, 255, 0, 255, 0, 255)
    }
}
