package com.aircalc.converter.presentation.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.usecase.ConvertToAirFryerUseCase
import com.aircalc.converter.domain.usecase.ConversionEstimate
import com.aircalc.converter.presentation.state.AirFryerUiState
import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for AirFryerViewModel.
 * Tests state management, user interactions, and business logic integration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AirFryerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: AirFryerViewModel
    private lateinit var mockUseCase: ConvertToAirFryerUseCase
    private lateinit var mockApplication: Application
    private lateinit var mockTimerManager: com.aircalc.converter.presentation.timer.TimerManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockUseCase = mockk()
        mockApplication = mockk(relaxed = true)
        mockTimerManager = mockk(relaxed = true)

        // Mock timer state flow
        every { mockTimerManager.timerState } returns kotlinx.coroutines.flow.MutableStateFlow(
            com.aircalc.converter.presentation.timer.TimerState()
        )

        // Mock restoreTimerState to do nothing
        coEvery { mockTimerManager.restoreTimerState(any()) } returns Unit

        // Mock the getQuickEstimate call that happens during ViewModel initialization
        every {
            mockUseCase.getQuickEstimate(
                temperature = any(),
                time = any(),
                category = any(),
                unit = any()
            )
        } returns ConversionEstimate(325, 18, TemperatureUnit.FAHRENHEIT)

        viewModel = AirFryerViewModel(mockApplication, mockUseCase, mockTimerManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial State Tests

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value

        assertThat(state.ovenTemperature).isEqualTo(180)
        assertThat(state.cookingTime).isEqualTo(25)
        assertThat(state.selectedCategory).isEqualTo(FoodCategory.READY_MEALS)
        assertThat(state.temperatureUnit).isEqualTo(TemperatureUnit.CELSIUS)
        assertThat(state.isConverting).isFalse()
        assertThat(state.conversionResult).isNull()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `canConvert is true initially with valid defaults`() = testScope.runTest {
        val canConvert = viewModel.canConvert.value
        assertThat(canConvert).isTrue()
    }

    @Test
    fun `conversionEstimate is not null initially with valid defaults`() = testScope.runTest {
        val estimate = viewModel.conversionEstimate.value
        assertThat(estimate).isNotNull()
    }

    // MARK: - Temperature Update Tests

    @Test
    fun `updateTemperature updates state correctly`() {
        val newTemperature = 375

        viewModel.updateTemperature(newTemperature)

        assertThat(viewModel.uiState.value.ovenTemperature).isEqualTo(newTemperature)
    }

    @Test
    fun `updateTemperature triggers canConvert recalculation`() = testScope.runTest {
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        testScope.advanceUntilIdle()

        assertThat(viewModel.canConvert.value).isTrue()
    }

    @Test
    fun `updateTemperature triggers conversionEstimate recalculation`() = testScope.runTest {
        val defaultEstimate = ConversionEstimate(325, 18, TemperatureUnit.FAHRENHEIT)
        val expectedEstimate = ConversionEstimate(350, 20, TemperatureUnit.FAHRENHEIT)

        // Mock for default values
        every {
            mockUseCase.getQuickEstimate(350, 25, FoodCategory.READY_MEALS, TemperatureUnit.FAHRENHEIT)
        } returns defaultEstimate

        every {
            mockUseCase.getQuickEstimate(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        } returns expectedEstimate

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        testScope.advanceUntilIdle()

        assertThat(viewModel.conversionEstimate.value).isEqualTo(expectedEstimate)
    }

    // MARK: - Cooking Time Update Tests

    @Test
    fun `updateCookingTime updates state correctly`() {
        val newTime = 30

        viewModel.updateCookingTime(newTime)

        assertThat(viewModel.uiState.value.cookingTime).isEqualTo(newTime)
    }

    @Test
    fun `updateCookingTime with invalid time affects canConvert`() = testScope.runTest {
        viewModel.updateTemperature(375)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)
        viewModel.updateCookingTime(0) // Invalid time

        testScope.advanceUntilIdle()

        assertThat(viewModel.canConvert.value).isFalse()
    }

    // MARK: - Category Selection Tests

    @Test
    fun `updateSelectedCategory updates state correctly`() {
        val category = FoodCategory.FRESH_VEGETABLES

        viewModel.updateSelectedCategory(category)

        assertThat(viewModel.uiState.value.selectedCategory).isEqualTo(category)
    }

    @Test
    fun `updateSelectedCategory enables conversion when all fields valid`() = testScope.runTest {
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        testScope.advanceUntilIdle()

        assertThat(viewModel.canConvert.value).isTrue()
    }

    // MARK: - Temperature Unit Tests

    @Test
    fun `updateTemperatureUnit changes unit without conversion when same value`() {
        viewModel.updateTemperature(375)

        viewModel.updateTemperatureUnit(TemperatureUnit.FAHRENHEIT)

        val state = viewModel.uiState.value
        assertThat(state.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
        assertThat(state.ovenTemperature).isEqualTo(375)
    }

    @Test
    fun `updateTemperatureUnit converts temperature when changing units`() {
        viewModel.updateTemperature(375) // Start with Fahrenheit

        viewModel.updateTemperatureUnit(TemperatureUnit.CELSIUS)

        val state = viewModel.uiState.value
        assertThat(state.temperatureUnit).isEqualTo(TemperatureUnit.CELSIUS)
        assertThat(state.ovenTemperature).isEqualTo(190) // 375°F ≈ 190°C
    }

    @Test
    fun `updateTemperatureUnit converts from Celsius to Fahrenheit correctly`() {
        // Start by setting to Celsius mode first
        viewModel.updateTemperatureUnit(TemperatureUnit.CELSIUS)
        // Then set a Celsius temperature
        viewModel.updateTemperature(200)

        // Now convert to Fahrenheit
        viewModel.updateTemperatureUnit(TemperatureUnit.FAHRENHEIT)

        val state = viewModel.uiState.value
        assertThat(state.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
        assertThat(state.ovenTemperature).isEqualTo(392) // 200°C = 392°F
    }

    // MARK: - Conversion Tests

    // Note: Test for null category removed since defaults always provide a category

    @Test
    fun `convertToAirFryer sets loading state initially`() = testScope.runTest {
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        val result = conversionResult()

        coEvery { mockUseCase.execute(input) } returns Result.success(result)

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        viewModel.convertToAirFryer()

        // Check immediate state
        assertThat(viewModel.uiState.value.isConverting).isTrue()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `convertToAirFryer handles successful conversion`() = testScope.runTest {
        val result = conversionResult(
            originalTemp = 375,
            originalTime = 25,
            airFryerTemp = 350,
            airFryerTime = 20
        )
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)

        coEvery { mockUseCase.execute(input) } returns Result.success(result)

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        viewModel.convertToAirFryer()
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isConverting).isFalse()
        assertThat(state.conversionResult).isEqualTo(result)
        assertThat(state.errorMessage).isNull()

        coVerify { mockUseCase.execute(input) }
    }

    @Test
    fun `convertToAirFryer handles conversion failure`() = testScope.runTest {
        val exception = Exception("Conversion failed")
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)

        coEvery { mockUseCase.execute(input) } returns Result.failure(exception)

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        viewModel.convertToAirFryer()
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isConverting).isFalse()
        assertThat(state.conversionResult).isNull()
        assertThat(state.errorMessage).isEqualTo("Conversion failed")
    }

    @Test
    fun `convertToAirFryer handles unexpected exception`() = testScope.runTest {
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)

        coEvery { mockUseCase.execute(input) } throws RuntimeException("Unexpected error")

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        viewModel.convertToAirFryer()
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isConverting).isFalse()
        assertThat(state.errorMessage).contains("An unexpected error occurred")
    }

    // MARK: - State Management Tests

    @Test
    fun `clearResult resets conversion state`() {
        // Set up state with conversion result by performing a conversion first
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        // Mock successful conversion to set state
        val result = conversionResult()
        coEvery { mockUseCase.execute(any()) } returns Result.success(result)
        viewModel.convertToAirFryer()
        testScope.advanceUntilIdle()

        viewModel.clearResult()

        val newState = viewModel.uiState.value
        assertThat(newState.conversionResult).isNull()
        assertThat(newState.errorMessage).isNull()
        assertThat(newState.ovenTemperature).isEqualTo(375) // Other state preserved
    }

    @Test
    fun `dismissError clears error message`() = testScope.runTest {
        // Set up valid inputs
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        // Mock a failed conversion
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        coEvery { mockUseCase.execute(input) } returns Result.failure(Exception("Conversion failed"))

        viewModel.convertToAirFryer()

        testScope.advanceUntilIdle()

        // Verify error exists
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.dismissError()

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    // MARK: - Accessibility Tests

    @Test
    fun `accessibility announcements update state correctly`() = testScope.runTest {
        val result = conversionResult()
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)

        coEvery { mockUseCase.execute(input) } returns Result.success(result)

        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)

        val initialAnnouncementId = viewModel.uiState.value.announcementId

        viewModel.convertToAirFryer()
        testScope.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.accessibilityAnnouncement).contains("Conversion completed successfully")
        assertThat(state.announcementId).isGreaterThan(initialAnnouncementId)
    }

    // MARK: - Derived State Tests

    @Test
    fun `canConvert requires all valid inputs`() = testScope.runTest {
        // Start with valid defaults
        assertThat(viewModel.canConvert.value).isTrue()

        // Set invalid temperature
        viewModel.updateTemperature(0)
        testScope.advanceUntilIdle()
        assertThat(viewModel.canConvert.value).isFalse()

        // Fix temperature
        viewModel.updateTemperature(375)
        testScope.advanceUntilIdle()
        assertThat(viewModel.canConvert.value).isTrue()

        // Set invalid time
        viewModel.updateCookingTime(0)
        testScope.advanceUntilIdle()
        assertThat(viewModel.canConvert.value).isFalse()

        // Fix time - should be valid again
        viewModel.updateCookingTime(25)
        testScope.advanceUntilIdle()
        assertThat(viewModel.canConvert.value).isTrue()
    }

    @Test
    fun `canConvert is false when converting`() = testScope.runTest {
        // Set up valid inputs
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)
        testScope.advanceUntilIdle()
        assertThat(viewModel.canConvert.value).isTrue()

        // Mock a slow conversion
        val input = ConversionInput(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        coEvery { mockUseCase.execute(input) } coAnswers {
            kotlinx.coroutines.delay(2000)
            Result.success(conversionResult())
        }

        viewModel.convertToAirFryer()

        // Advance time partially to allow state change but not complete the conversion
        testScope.advanceTimeBy(100)

        // During conversion, canConvert should be false
        assertThat(viewModel.canConvert.value).isFalse()
    }

    @Test
    fun `conversionEstimate updates when inputs change`() = testScope.runTest {
        val estimateDefault = ConversionEstimate(325, 18, TemperatureUnit.FAHRENHEIT)
        val estimate1 = ConversionEstimate(350, 20, TemperatureUnit.FAHRENHEIT)
        val estimate2 = ConversionEstimate(325, 15, TemperatureUnit.FAHRENHEIT)

        // Mock for default values (350°F, 25min, READY_MEALS)
        every {
            mockUseCase.getQuickEstimate(350, 25, FoodCategory.READY_MEALS, TemperatureUnit.FAHRENHEIT)
        } returns estimateDefault

        every {
            mockUseCase.getQuickEstimate(375, 25, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        } returns estimate1

        every {
            mockUseCase.getQuickEstimate(350, 20, FoodCategory.FROZEN_FOODS, TemperatureUnit.FAHRENHEIT)
        } returns estimate2

        // Set initial values
        viewModel.updateTemperature(375)
        viewModel.updateCookingTime(25)
        viewModel.updateSelectedCategory(FoodCategory.FROZEN_FOODS)
        testScope.advanceUntilIdle()

        assertThat(viewModel.conversionEstimate.value).isEqualTo(estimate1)

        // Change temperature
        viewModel.updateTemperature(350)
        viewModel.updateCookingTime(20)
        testScope.advanceUntilIdle()

        assertThat(viewModel.conversionEstimate.value).isEqualTo(estimate2)
    }
}