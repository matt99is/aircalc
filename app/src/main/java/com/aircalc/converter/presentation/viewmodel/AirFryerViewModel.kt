package com.aircalc.converter.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.usecase.ConvertToAirFryerUseCase
import com.aircalc.converter.domain.usecase.ConversionEstimate
import com.aircalc.converter.presentation.state.AirFryerUiState
import com.aircalc.converter.presentation.timer.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Air Fryer app.
 * Manages UI state and coordinates between UI and domain layer.
 * Extends AndroidViewModel to safely manage MediaPlayer across configuration changes.
 */
@HiltViewModel
class AirFryerViewModel @Inject constructor(
    application: Application,
    private val convertToAirFryerUseCase: ConvertToAirFryerUseCase,
    private val timerManager: TimerManager
) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer? = null

    private val _uiState = MutableStateFlow(AirFryerUiState())
    val uiState: StateFlow<AirFryerUiState> = _uiState.asStateFlow()

    // Timer state from TimerManager
    val timerState = timerManager.timerState

    init {
        // Observe timer state changes to trigger alarm when finished
        viewModelScope.launch {
            timerState.collect { state ->
                if (state.isFinished && state.remainingSeconds == 0) {
                    playTimerAlarm()
                }
            }
        }
    }

    // Derived states for UI optimization
    val canConvert: StateFlow<Boolean> = _uiState.map { state ->
        !state.isConverting &&
        state.temperatureUnit.isValidTemperature(state.ovenTemperature) &&
        state.cookingTime in 1..300 &&
        state.selectedCategory != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AirFryerUiState().let { state ->
            !state.isConverting &&
            state.temperatureUnit.isValidTemperature(state.ovenTemperature) &&
            state.cookingTime in 1..300 &&
            state.selectedCategory != null
        }
    )

    val conversionEstimate: StateFlow<ConversionEstimate?> = _uiState.map { state ->
        if (state.selectedCategory != null && state.ovenTemperature > 0 && state.cookingTime > 0) {
            convertToAirFryerUseCase.getQuickEstimate(
                temperature = state.ovenTemperature,
                time = state.cookingTime,
                category = state.selectedCategory,
                unit = state.temperatureUnit
            )
        } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AirFryerUiState().let { state ->
            val category = state.selectedCategory
            if (category != null && state.ovenTemperature > 0 && state.cookingTime > 0) {
                convertToAirFryerUseCase.getQuickEstimate(
                    temperature = state.ovenTemperature,
                    time = state.cookingTime,
                    category = category,
                    unit = state.temperatureUnit
                )
            } else null
        }
    )

    /**
     * Update oven temperature.
     */
    fun updateTemperature(temperature: Int) {
        _uiState.value = _uiState.value.copy(ovenTemperature = temperature)
    }

    /**
     * Update cooking time.
     */
    fun updateCookingTime(time: Int) {
        _uiState.value = _uiState.value.copy(cookingTime = time)
    }

    /**
     * Update selected food category.
     */
    fun updateSelectedCategory(category: FoodCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * Update temperature unit.
     */
    fun updateTemperatureUnit(unit: TemperatureUnit) {
        val currentState = _uiState.value

        // Convert current temperature to new unit if needed
        val convertedTemp = if (currentState.temperatureUnit != unit) {
            currentState.temperatureUnit.convertTo(currentState.ovenTemperature, unit)
        } else {
            currentState.ovenTemperature
        }

        _uiState.value = currentState.copy(
            temperatureUnit = unit,
            ovenTemperature = convertedTemp
        )
    }

    /**
     * Perform conversion to air fryer settings.
     */
    fun convertToAirFryer() {
        val currentState = _uiState.value

        // Check if we have all required data
        val selectedCategory = currentState.selectedCategory
        if (selectedCategory == null) {
            showError("Please select a food category")
            return
        }

        _uiState.value = currentState.copy(
            isConverting = true,
            errorMessage = null
        )

        announceToAccessibility("Converting oven settings to air fryer settings. Please wait.")

        viewModelScope.launch {
            try {
                val input = ConversionInput(
                    ovenTemperature = currentState.ovenTemperature,
                    cookingTimeMinutes = currentState.cookingTime,
                    foodCategory = selectedCategory,
                    temperatureUnit = currentState.temperatureUnit
                )

                convertToAirFryerUseCase.execute(input)
                    .onSuccess { result ->
                        _uiState.value = _uiState.value.copy(
                            isConverting = false,
                            conversionResult = result,
                            errorMessage = null
                        )
                        announceConversionResult(result)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isConverting = false,
                            errorMessage = error.message ?: "Conversion failed"
                        )
                        announceToAccessibility("Conversion failed: ${error.message}")
                    }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConverting = false,
                    errorMessage = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear conversion result and reset form.
     */
    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            conversionResult = null,
            errorMessage = null
        )
        timerManager.resetTimer()
    }

    /**
     * Dismiss error message.
     */
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Timer management functions.
     */
    fun startTimer(minutes: Int) {
        timerManager.startTimer(viewModelScope, minutes)
        announceToAccessibility("Timer started for $minutes minutes")
    }

    fun pauseTimer() {
        timerManager.pauseTimer()
        announceToAccessibility("Timer paused")
    }

    fun resumeTimer() {
        timerManager.resumeTimer()
        announceToAccessibility("Timer resumed")
    }

    fun resetTimer() {
        timerManager.resetTimer()
        announceToAccessibility("Timer reset")
    }

    /**
     * Accessibility announcements.
     */
    private fun announceConversionResult(result: ConversionResult) {
        val announcement = "Conversion completed successfully. " +
                "Air fryer temperature: ${result.getFormattedAirFryerTemp()}. " +
                "Cooking time: ${result.airFryerTimeMinutes} minutes. " +
                "Tip: ${result.cookingTip}"

        announceToAccessibility(announcement)
    }

    private fun announceToAccessibility(message: String) {
        _uiState.value = _uiState.value.copy(
            accessibilityAnnouncement = message,
            announcementId = _uiState.value.announcementId + 1
        )
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
        announceToAccessibility("Error: $message")
    }

    /**
     * Play alarm sound and vibrate when timer finishes.
     * Manages MediaPlayer lifecycle to prevent memory leaks across configuration changes.
     */
    private fun playTimerAlarm() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext

                // Vibrate
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Pattern: vibrate for 500ms, pause 200ms, vibrate 500ms, pause 200ms, vibrate 500ms
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                }

                // Play alarm sound using MediaPlayer
                withContext(Dispatchers.Main) {
                    try {
                        // Release any existing player to prevent memory leaks
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            // Use default alarm sound
                            setDataSource(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)

                            // Set audio attributes to use alarm stream
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                setAudioStreamType(AudioManager.STREAM_ALARM)
                            }

                            // Set volume to maximum
                            setVolume(1.0f, 1.0f)

                            // Prepare and play
                            prepare()
                            start()

                            // Set completion listener to release resources
                            setOnCompletionListener { mp ->
                                mp.release()
                                mediaPlayer = null
                            }
                        }

                        // Stop after 5 seconds if still playing
                        delay(5000)
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                mp.stop()
                            }
                            mp.release()
                            mediaPlayer = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Cleanup on error
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }

                announceToAccessibility("Timer finished! Your food is ready.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up MediaPlayer to prevent memory leaks
        mediaPlayer?.release()
        mediaPlayer = null
        timerManager.cleanup()
    }
}