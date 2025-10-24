package com.aircalc.converter.data.repository

import com.aircalc.converter.data.datasource.ConversionDataSource
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.repository.ConversionRepository
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
            // Check cache first, gracefully handle cache errors
            val cacheKey = generateCacheKey(input)
            val cachedResult = try {
                conversionCache.get(cacheKey)
            } catch (e: Exception) {
                // Log cache error but continue with conversion
                null
            }

            if (cachedResult != null) {
                return Result.success(cachedResult)
            }

            // Perform conversion calculation
            val result = dataSource.performConversion(input)

            // Cache the result, gracefully handle cache errors
            try {
                conversionCache.put(cacheKey, result)
            } catch (e: Exception) {
                // Log cache error but don't fail the conversion
            }

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
 * True LRU cache for conversion results using LinkedHashMap with access-order.
 */
@Singleton
class ConversionCache @Inject constructor() {
    private val maxSize = 50

    private val cache = object : LinkedHashMap<String, ConversionResult>(
        16,
        0.75f,
        true  // access-order mode for proper LRU behavior
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, ConversionResult>): Boolean {
            return size > maxSize
        }
    }

    @Synchronized
    fun get(key: String): ConversionResult? = cache[key]

    @Synchronized
    fun put(key: String, result: ConversionResult) {
        cache[key] = result
    }

    @Synchronized
    fun clear() {
        cache.clear()
    }

    @Synchronized
    fun size(): Int = cache.size
}