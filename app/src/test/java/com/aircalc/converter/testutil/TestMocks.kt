package com.aircalc.converter.testutil

import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.repository.ConversionRepository
import com.aircalc.converter.domain.usecase.ConversionValidator
import com.aircalc.converter.data.datasource.ConversionDataSource
import io.mockk.*

/**
 * Mock factory for creating test doubles.
 */
object TestMocks {

    /**
     * Create a mock ConversionRepository with default behavior.
     */
    fun createMockRepository(
        conversionResult: ConversionResult? = null,
        shouldSucceed: Boolean = true
    ): ConversionRepository = mockk {
        val result = conversionResult ?: TestData.Common.input375F25min.let { input ->
            conversionResult(
                originalTemp = input.ovenTemperature,
                originalTime = input.cookingTimeMinutes,
                airFryerTemp = input.ovenTemperature - 25,
                airFryerTime = (input.cookingTimeMinutes * 0.8).toInt(),
                unit = input.temperatureUnit,
                category = input.foodCategory
            )
        }

        coEvery { convertToAirFryer(any()) } returns if (shouldSucceed) {
            Result.success(result)
        } else {
            Result.failure(Exception("Test failure"))
        }

        coEvery { getFoodCategories() } returns Result.success(FoodCategory.getAllCategories())
        coEvery { getFoodCategory(any()) } answers {
            val id = firstArg<String>()
            Result.success(FoodCategory.getById(id))
        }
        coEvery { saveConversionHistory(any()) } returns Result.success(Unit)
        coEvery { getConversionHistory() } returns Result.success(emptyList())
        coEvery { clearCache() } returns Result.success(Unit)
    }

    /**
     * Create a mock ConversionValidator with configurable behavior.
     */
    fun createMockValidator(
        isValid: Boolean = true,
        errors: List<ValidationError> = emptyList()
    ): ConversionValidator = mockk {
        every { validateInput(any()) } returns if (isValid) {
            ConversionValidation.Valid
        } else {
            ConversionValidation.Invalid(errors)
        }

        every { validateResult(any()) } returns ConversionValidation.Valid
    }

    /**
     * Create a mock ConversionDataSource with default behavior.
     */
    fun createMockDataSource(): ConversionDataSource = mockk {
        every { performConversion(any()) } answers {
            val input = firstArg<ConversionInput>()
            val tempReduction = input.temperatureUnit.convertTempReduction(
                input.foodCategory.tempReductionFahrenheit
            )
            val airFryerTemp = input.ovenTemperature - tempReduction
            val airFryerTime = (input.cookingTimeMinutes * input.foodCategory.timeMultiplier).toInt()

            ConversionResult(
                originalTemperature = input.ovenTemperature,
                originalTime = input.cookingTimeMinutes,
                airFryerTemperature = airFryerTemp,
                airFryerTimeMinutes = airFryerTime,
                temperatureUnit = input.temperatureUnit,
                foodCategory = input.foodCategory,
                cookingTip = input.foodCategory.cookingTip,
                temperatureReduction = tempReduction,
                timeReduction = input.cookingTimeMinutes - airFryerTime
            )
        }

        every { getFoodCategories() } returns FoodCategory.getAllCategories()
        every { getFoodCategory(any()) } answers {
            val id = firstArg<String>()
            FoodCategory.getById(id)
        }
        every { saveToHistory(any()) } returns Unit
        every { getHistory() } returns emptyList()
        every { clearHistory() } returns Unit
    }

    /**
     * Creates validation errors for testing.
     */
    object ValidationErrors {
        val temperatureTooLow = ValidationError.TemperatureTooLow
        val temperatureTooHigh = ValidationError.TemperatureTooHigh
        val timeTooShort = ValidationError.TimeTooShort
        val timeTooLong = ValidationError.TimeTooLong
        val meatTempTooLow = ValidationError.Custom("Temperature too low for raw meat - food safety concern")
        val bakedGoodsTempTooHigh = ValidationError.Custom("Temperature too high for baked goods - may burn")
        val vegetablesTempTooLow = ValidationError.Custom("Temperature too low for vegetables - may not cook properly")
    }
}