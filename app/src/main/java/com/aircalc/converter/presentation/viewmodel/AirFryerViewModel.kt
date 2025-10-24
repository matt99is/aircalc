package com.aircalc.converter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 */
@HiltViewModel
class AirFryerViewModel @Inject constructor(
    private val convertToAirFryerUseCase: ConvertToAirFryerUseCase,
    private val timerManager: TimerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AirFryerUiState())
    val uiState: StateFlow<AirFryerUiState> = _uiState.asStateFlow()

    // Timer state from TimerManager
    val timerState = timerManager.timerState

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

    override fun onCleared() {
        super.onCleared()
        timerManager.cleanup()
    }
}