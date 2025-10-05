package com.aircalc.converter.data.repository

import com.aircalc.converter.data.datasource.ConversionDataSource
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.repository.ConversionRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ConversionRepository.
 * Handles data operations and caching logic.
 */
@Singleton
class ConversionRepositoryImpl @Inject constructor(
    private val dataSource: ConversionDataSource,
    private val conversionCache: ConversionCache
) : ConversionRepository {

    override suspend fun convertToAirFryer(input: ConversionInput): Result<ConversionResult> {
        return try {
            // Check cache first
            val cacheKey = generateCacheKey(input)
            conversionCache.get(cacheKey)?.let { cachedResult ->
                return Result.success(cachedResult)
            }

            // Simulate processing delay for realistic UX
            delay(500)

            // Perform conversion calculation
            val result = dataSource.performConversion(input)

            // Cache the result
            conversionCache.put(cacheKey, result)

            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodCategories(): Result<List<FoodCategory>> {
        return try {
            val categories = dataSource.getFoodCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodCategory(id: String): Result<FoodCategory?> {
        return try {
            val category = dataSource.getFoodCategory(id)
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveConversionHistory(result: ConversionResult): Result<Unit> {
        return try {
            dataSource.saveToHistory(result)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversionHistory(): Result<List<ConversionResult>> {
        return try {
            val history = dataSource.getHistory()
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            conversionCache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateCacheKey(input: ConversionInput): String {
        return "${input.ovenTemperature}-${input.cookingTimeMinutes}-${input.foodCategory.id}-${input.temperatureUnit.name}"
    }
}

/**
 * Simple in-memory cache for conversion results.
 */
@Singleton
class ConversionCache @Inject constructor() {
    private val cache = mutableMapOf<String, ConversionResult>()
    private val maxSize = 50

    fun get(key: String): ConversionResult? = cache[key]

    fun put(key: String, result: ConversionResult) {
        if (cache.size >= maxSize) {
            // Remove oldest entry (simple LRU approximation)
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)
        }
        cache[key] = result
    }

    fun clear() {
        cache.clear()
    }

    fun size(): Int = cache.size
}