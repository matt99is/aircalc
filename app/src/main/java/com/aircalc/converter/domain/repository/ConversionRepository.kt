package com.aircalc.converter.domain.repository

import com.aircalc.converter.domain.model.ConversionInput
import com.aircalc.converter.domain.model.ConversionResult
import com.aircalc.converter.domain.model.FoodCategory

/**
 * Repository interface for conversion operations.
 * This interface defines the contract for data operations without implementation details.
 */
interface ConversionRepository {

    /**
     * Perform air fryer conversion calculation.
     */
    suspend fun convertToAirFryer(input: ConversionInput): Result<ConversionResult>

    /**
     * Get all available food categories.
     */
    suspend fun getFoodCategories(): Result<List<FoodCategory>>

    /**
     * Get a specific food category by ID.
     */
    suspend fun getFoodCategory(id: String): Result<FoodCategory?>

    /**
     * Save conversion result for history (if needed in future).
     */
    suspend fun saveConversionHistory(result: ConversionResult): Result<Unit>

    /**
     * Get conversion history (if needed in future).
     */
    suspend fun getConversionHistory(): Result<List<ConversionResult>>

    /**
     * Clear conversion cache.
     */
    suspend fun clearCache(): Result<Unit>
}