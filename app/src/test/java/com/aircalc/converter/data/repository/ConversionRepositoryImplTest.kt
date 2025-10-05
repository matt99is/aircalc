package com.aircalc.converter.data.repository

import com.aircalc.converter.data.datasource.ConversionDataSource
import com.aircalc.converter.domain.model.*
import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConversionRepositoryImpl.
 * Tests caching behavior, error handling, and data source integration.
 */
class ConversionRepositoryImplTest {

    private lateinit var repository: ConversionRepositoryImpl
    private lateinit var mockDataSource: ConversionDataSource
    private lateinit var mockCache: ConversionCache

    @Before
    fun setUp() {
        mockDataSource = TestMocks.createMockDataSource()
        mockCache = mockk()
        repository = ConversionRepositoryImpl(mockDataSource, mockCache)
    }

    // MARK: - Conversion Tests

    @Test
    fun `convertToAirFryer returns cached result when available`() = runTest {
        val input = TestData.Common.input375F25min
        val cachedResult = conversionResult()
        val cacheKey = "375-25-frozen_foods-FAHRENHEIT"

        every { mockCache.get(cacheKey) } returns cachedResult

        val result = repository.convertToAirFryer(input)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(cachedResult)

        verify { mockCache.get(cacheKey) }
        verify(exactly = 0) { mockDataSource.performConversion(any()) }
        verify(exactly = 0) { mockCache.put(any(), any()) }
    }

    @Test
    fun `convertToAirFryer performs conversion when cache miss`() = runTest {
        val input = TestData.Common.input375F25min
        val conversionResult = conversionResult()
        val cacheKey = "375-25-frozen_foods-FAHRENHEIT"

        every { mockCache.get(cacheKey) } returns null
        every { mockDataSource.performConversion(input) } returns conversionResult
        every { mockCache.put(cacheKey, conversionResult) } returns Unit

        val result = repository.convertToAirFryer(input)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(conversionResult)

        verify { mockCache.get(cacheKey) }
        verify { mockDataSource.performConversion(input) }
        verify { mockCache.put(cacheKey, conversionResult) }
    }

    @Test
    fun `convertToAirFryer generates correct cache key`() = runTest {
        val input = ConversionInputBuilder()
            .withTemperature(400)
            .withTime(30)
            .withCategory(FoodCategory.RAW_MEATS)
            .withUnit(TemperatureUnit.CELSIUS)
            .build()
        val expectedCacheKey = "400-30-raw_meats-CELSIUS"

        every { mockCache.get(expectedCacheKey) } returns null
        every { mockDataSource.performConversion(input) } returns conversionResult()
        every { mockCache.put(expectedCacheKey, any()) } returns Unit

        repository.convertToAirFryer(input)

        verify { mockCache.get(expectedCacheKey) }
        verify { mockCache.put(expectedCacheKey, any()) }
    }

    @Test
    fun `convertToAirFryer handles data source exception`() = runTest {
        val input = TestData.Common.input375F25min
        val exception = RuntimeException("Data source error")

        every { mockCache.get(any()) } returns null
        every { mockDataSource.performConversion(input) } throws exception

        val result = repository.convertToAirFryer(input)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        verify { mockCache.get(any()) }
        verify { mockDataSource.performConversion(input) }
        verify(exactly = 0) { mockCache.put(any(), any()) }
    }

    @Test
    fun `convertToAirFryer handles cache exception gracefully`() = runTest {
        val input = TestData.Common.input375F25min
        val conversionResult = conversionResult()

        every { mockCache.get(any()) } throws RuntimeException("Cache error")
        every { mockDataSource.performConversion(input) } returns conversionResult
        every { mockCache.put(any(), conversionResult) } returns Unit

        val result = repository.convertToAirFryer(input)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(conversionResult)

        verify { mockDataSource.performConversion(input) }
        verify { mockCache.put(any(), conversionResult) }
    }

    // MARK: - Food Category Tests

    @Test
    fun `getFoodCategories returns success with all categories`() = runTest {
        val categories = FoodCategory.getAllCategories()
        every { mockDataSource.getFoodCategories() } returns categories

        val result = repository.getFoodCategories()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(categories)
        verify { mockDataSource.getFoodCategories() }
    }

    @Test
    fun `getFoodCategories handles data source exception`() = runTest {
        val exception = RuntimeException("Categories error")
        every { mockDataSource.getFoodCategories() } throws exception

        val result = repository.getFoodCategories()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `getFoodCategory returns success with found category`() = runTest {
        val categoryId = "frozen_foods"
        val category = FoodCategory.FROZEN_FOODS
        every { mockDataSource.getFoodCategory(categoryId) } returns category

        val result = repository.getFoodCategory(categoryId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(category)
        verify { mockDataSource.getFoodCategory(categoryId) }
    }

    @Test
    fun `getFoodCategory returns success with null for non-existent category`() = runTest {
        val categoryId = "non_existent"
        every { mockDataSource.getFoodCategory(categoryId) } returns null

        val result = repository.getFoodCategory(categoryId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
        verify { mockDataSource.getFoodCategory(categoryId) }
    }

    @Test
    fun `getFoodCategory handles data source exception`() = runTest {
        val categoryId = "frozen_foods"
        val exception = RuntimeException("Category error")
        every { mockDataSource.getFoodCategory(categoryId) } throws exception

        val result = repository.getFoodCategory(categoryId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    // MARK: - History Tests

    @Test
    fun `saveConversionHistory returns success when data source succeeds`() = runTest {
        val conversionResult = conversionResult()
        every { mockDataSource.saveToHistory(conversionResult) } returns Unit

        val result = repository.saveConversionHistory(conversionResult)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        verify { mockDataSource.saveToHistory(conversionResult) }
    }

    @Test
    fun `saveConversionHistory handles data source exception`() = runTest {
        val conversionResult = conversionResult()
        val exception = RuntimeException("Save error")
        every { mockDataSource.saveToHistory(conversionResult) } throws exception

        val result = repository.saveConversionHistory(conversionResult)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `getConversionHistory returns success with history list`() = runTest {
        val history = listOf(conversionResult(), conversionResult())
        every { mockDataSource.getHistory() } returns history

        val result = repository.getConversionHistory()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(history)
        verify { mockDataSource.getHistory() }
    }

    @Test
    fun `getConversionHistory handles data source exception`() = runTest {
        val exception = RuntimeException("History error")
        every { mockDataSource.getHistory() } throws exception

        val result = repository.getConversionHistory()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    // MARK: - Cache Management Tests

    @Test
    fun `clearCache returns success when cache clears successfully`() = runTest {
        every { mockCache.clear() } returns Unit

        val result = repository.clearCache()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        verify { mockCache.clear() }
    }

    @Test
    fun `clearCache handles cache exception`() = runTest {
        val exception = RuntimeException("Clear cache error")
        every { mockCache.clear() } throws exception

        val result = repository.clearCache()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}

/**
 * Unit tests for ConversionCache.
 * Tests cache behavior, LRU eviction, and boundary conditions.
 */
class ConversionCacheTest {

    private lateinit var cache: ConversionCache

    @Before
    fun setUp() {
        cache = ConversionCache()
    }

    @Test
    fun `get returns null for non-existent key`() {
        val result = cache.get("non_existent_key")

        assertThat(result).isNull()
    }

    @Test
    fun `put and get stores and retrieves value correctly`() {
        val key = "test_key"
        val value = conversionResult()

        cache.put(key, value)
        val result = cache.get(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun `put overwrites existing value for same key`() {
        val key = "test_key"
        val value1 = conversionResult(originalTemp = 350)
        val value2 = conversionResult(originalTemp = 375)

        cache.put(key, value1)
        cache.put(key, value2)
        val result = cache.get(key)

        assertThat(result).isEqualTo(value2)
        assertThat(cache.size()).isEqualTo(1)
    }

    @Test
    fun `cache evicts oldest entry when max size exceeded`() {
        // Fill cache to max capacity (50)
        repeat(50) { index ->
            cache.put("key_$index", conversionResult(originalTemp = 300 + index))
        }

        assertThat(cache.size()).isEqualTo(50)
        assertThat(cache.get("key_0")).isNotNull() // First entry still exists

        // Add one more to trigger eviction
        cache.put("key_50", conversionResult(originalTemp = 350))

        assertThat(cache.size()).isEqualTo(50)
        assertThat(cache.get("key_0")).isNull()    // First entry evicted
        assertThat(cache.get("key_50")).isNotNull() // New entry exists
        assertThat(cache.get("key_1")).isNotNull()  // Second entry still exists
    }

    @Test
    fun `clear removes all entries`() {
        cache.put("key1", conversionResult())
        cache.put("key2", conversionResult())

        assertThat(cache.size()).isEqualTo(2)

        cache.clear()

        assertThat(cache.size()).isEqualTo(0)
        assertThat(cache.get("key1")).isNull()
        assertThat(cache.get("key2")).isNull()
    }

    @Test
    fun `size returns correct count`() {
        assertThat(cache.size()).isEqualTo(0)

        cache.put("key1", conversionResult())
        assertThat(cache.size()).isEqualTo(1)

        cache.put("key2", conversionResult())
        assertThat(cache.size()).isEqualTo(2)

        cache.clear()
        assertThat(cache.size()).isEqualTo(0)
    }

    @Test
    fun `cache handles many operations efficiently`() {
        // Add many entries
        repeat(100) { index ->
            cache.put("key_$index", conversionResult(originalTemp = 300 + index))
        }

        // Verify cache maintained max size
        assertThat(cache.size()).isEqualTo(50)

        // Verify recent entries are still accessible
        assertThat(cache.get("key_99")).isNotNull()
        assertThat(cache.get("key_50")).isNotNull()

        // Verify old entries were evicted
        assertThat(cache.get("key_0")).isNull()
        assertThat(cache.get("key_49")).isNull()
    }
}