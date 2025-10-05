package com.aircalc.converter.domain.usecase

import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.repository.ConversionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for converting oven recipes to air fryer settings.
 * Contains business logic and validation rules.
 */
@Singleton
class ConvertToAirFryerUseCase @Inject constructor(
    private val repository: ConversionRepository,
    private val validator: ConversionValidator
) {

    /**
     * Execute the conversion with validation.
     */
    suspend fun execute(input: ConversionInput): Result<ConversionResult> {
        return try {
            // Validate input
            val validation = validator.validateInput(input)
            if (!validation.isValid()) {
                val errors = (validation as ConversionValidation.Invalid).errors
                return Result.failure(
                    ConversionException("Validation failed: ${errors.joinToString { it.message }}")
                )
            }

            // Perform conversion
            repository.convertToAirFryer(input)

        } catch (e: Exception) {
            Result.failure(ConversionException("Conversion failed: ${e.message}", e))
        }
    }

    /**
     * Get quick conversion estimate without full validation (for preview).
     */
    fun getQuickEstimate(
        temperature: Int,
        time: Int,
        category: FoodCategory,
        unit: TemperatureUnit
    ): ConversionEstimate {
        val tempReduction = unit.convertTempReduction(category.tempReductionFahrenheit)
        val estimatedTemp = temperature - tempReduction
        val estimatedTime = (time * category.timeMultiplier).toInt()

        return ConversionEstimate(
            estimatedTemperature = estimatedTemp,
            estimatedTime = estimatedTime,
            temperatureUnit = unit
        )
    }
}

/**
 * Quick conversion estimate for real-time preview.
 */
data class ConversionEstimate(
    val estimatedTemperature: Int,
    val estimatedTime: Int,
    val temperatureUnit: TemperatureUnit
) {
    fun getFormattedTemperature(): String = "$estimatedTemperature${temperatureUnit.symbol}"
    fun getFormattedTime(): String = "$estimatedTime min"
}

/**
 * Custom exception for conversion errors.
 */
class ConversionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)