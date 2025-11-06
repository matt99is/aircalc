package com.aircalc.converter.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aircalc.converter.data.preferences.DisclaimerPreferences
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.usecase.ConvertToAirFryerUseCase
import com.aircalc.converter.domain.usecase.ConversionEstimate
import com.aircalc.converter.presentation.state.AirFryerUiState
import com.aircalc.converter.presentation.timer.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Air Fryer app.
 * Manages UI state and coordinates between UI and domain layer.
 * Uses TimerManager for alarm-based timer functionality.
 */
@HiltViewModel
class AirFryerViewModel @Inject constructor(
    application: Application,
    private val convertToAirFryerUseCase: ConvertToAirFryerUseCase,
    private val timerManager: TimerManager,
    private val disclaimerPreferences: DisclaimerPreferences
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AirFryerUiState())
    val uiState: StateFlow<AirFryerUiState> = _uiState.asStateFlow()

    // Timer state exposed from timer manager
    val timerState = timerManager.timerState

    // Disclaimer acceptance state - null means loading
    val isDisclaimerAccepted: StateFlow<Boolean?> = disclaimerPreferences.isDisclaimerAccepted
        .map<Boolean, Boolean?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        // Restore timer state if app was killed
        viewModelScope.launch {
            timerManager.restoreTimerState(viewModelScope)
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

    val conversionEstimate: StateFlow<ConversionEstimate?> = _uiState
        .map { state ->
            // Create a key from only the relevant fields for conversion
            ConversionInputKey(
                temperature = state.ovenTemperature,
                time = state.cookingTime,
                category = state.selectedCategory,
                unit = state.temperatureUnit
            )
        }
        .distinctUntilChanged() // Only recalculate when these specific fields change
        .map { key ->
            if (key.category != null && key.temperature > 0 && key.time > 0) {
                convertToAirFryerUseCase.getQuickEstimate(
                    temperature = key.temperature,
                    time = key.time,
                    category = key.category,
                    unit = key.unit
                )
            } else null
        }
        .stateIn(
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
     * Data class representing the inputs needed for conversion estimation.
     * Used to filter unnecessary recalculations when other state fields change.
     */
    private data class ConversionInputKey(
        val temperature: Int,
        val time: Int,
        val category: FoodCategory?,
        val unit: TemperatureUnit
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
        timerManager.resetTimer(viewModelScope)
    }

    /**
     * Dismiss error message.
     */
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Timer management functions using alarm-based timer.
     */
    fun startTimer(minutes: Int) {
        timerManager.startTimer(viewModelScope, minutes)
        announceToAccessibility("Timer started for $minutes minutes")
    }

    fun pauseTimer() {
        timerManager.pauseTimer(viewModelScope)
        announceToAccessibility("Timer paused")
    }

    fun resumeTimer() {
        timerManager.resumeTimer(viewModelScope)
        announceToAccessibility("Timer resumed")
    }

    fun resetTimer() {
        timerManager.resetTimer(viewModelScope)
        announceToAccessibility("Timer reset")
    }

    /**
     * Accept the disclaimer and save to preferences.
     */
    fun acceptDisclaimer() {
        viewModelScope.launch {
            disclaimerPreferences.setDisclaimerAccepted()
        }
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

    override fun onCleared() {
        super.onCleared()
        // Cleanup timer manager resources
        timerManager.cleanup()
    }
}