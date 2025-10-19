package com.aircalc.converter.domain.usecase

import com.aircalc.converter.domain.model.*
import com.aircalc.converter.domain.repository.ConversionRepository
import com.aircalc.converter.testutil.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConvertToAirFryerUseCase.
 * Tests business logic orchestration and error handling.
 */
class ConvertToAirFryerUseCaseTest {

    private lateinit var useCase: ConvertToAirFryerUseCase
    private lateinit var mockRepository: ConversionRepository
    private lateinit var mockValidator: ConversionValidator

    @Before
    fun setUp() {
        mockRepository = TestMocks.createMockRepository()
        mockValidator = TestMocks.createMockValidator()
        useCase = ConvertToAirFryerUseCase(mockRepository, mockValidator)
    }

    // MARK: - Execute Success Tests

    @Test
    fun `execute returns success when validation passes and repository succeeds`() = runTest {
        val input = TestData.Common.input375F25min
        val expectedResult = conversionResult(
            originalTemp = 375,
            originalTime = 25,
            airFryerTemp = 350,
            airFryerTime = 20
        )

        every { mockValidator.validateInput(input) } returns ConversionValidation.Valid
        coEvery { mockRepository.convertToAirFryer(input) } returns Result.success(expectedResult)

        val result = useCase.execute(input)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedResult)
        verify { mockValidator.validateInput(input) }
        coVerify { mockRepository.convertToAirFryer(input) }
    }

    @Test
    fun `execute processes all food categories correctly`() = runTest {
        val categories = FoodCategory.getAllCategories()

        categories.forEach { category ->
            val input = ConversionInputBuilder()
                .withCategory(category)
                .build()

            every { mockValidator.validateInput(input) } returns ConversionValidation.Valid

            val result = useCase.execute(input)

            assertThat(result.isSuccess).isTrue()
            verify { mockValidator.validateInput(input) }
        }
    }

    // MARK: - Execute Validation Failure Tests

    @Test
    fun `execute returns failure when validation fails with single error`() = runTest {
        val input = TestData.Common.input375F25min
        val validationError = ValidationError.TemperatureTooLow
        val validationResult = ConversionValidation.Invalid(listOf(validationError))

        every { mockValidator.validateInput(input) } returns validationResult

        val result = useCase.execute(input)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ConversionException::class.java)
        val exception = result.exceptionOrNull() as ConversionException
        assertThat(exception.message).contains("Validation failed")
        assertThat(exception.message).contains(validationError.message)

        verify { mockValidator.validateInput(input) }
        coVerify(exactly = 0) { mockRepository.convertToAirFryer(any()) }
    }

    @Test
    fun `execute returns failure when validation fails with multiple errors`() = runTest {
        val input = TestData.Common.input375F25min
        val errors = listOf(
            ValidationError.TemperatureTooLow,
            ValidationError.TimeTooShort
        )
        val validationResult = ConversionValidation.Invalid(errors)

        every { mockValidator.validateInput(input) } returns validationResult

        val result = useCase.execute(input)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as ConversionException
        assertThat(exception.message).contains("Validation failed")
        assertThat(exception.message).contains(ValidationError.TemperatureTooLow.message)
        assertThat(exception.message).contains(ValidationError.TimeTooShort.message)
    }

    // MARK: - Execute Repository Failure Tests

    @Test
    fun `execute returns failure when repository fails`() = runTest {
        val input = TestData.Common.input375F25min
        val repositoryException = Exception("Repository error")

        every { mockValidator.validateInput(input) } returns ConversionValidation.Valid
        coEvery { mockRepository.convertToAirFryer(input) } returns Result.failure(repositoryException)

        val result = useCase.execute(input)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(repositoryException)

        verify { mockValidator.validateInput(input) }
        coVerify { mockRepository.convertToAirFryer(input) }
    }

    @Test
    fun `execute handles unexpected exceptions gracefully`() = runTest {
        val input = TestData.Common.input375F25min
        val unexpectedException = RuntimeException("Unexpected error")

        every { mockValidator.validateInput(input) } throws unexpectedException

        val result = useCase.execute(input)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ConversionException::class.java)
        val exception = result.exceptionOrNull() as ConversionException
        assertThat(exception.message).contains("Conversion failed")
        assertThat(exception.cause).isEqualTo(unexpectedException)
    }

    // MARK: - Quick Estimate Tests

    @Test
    fun `getQuickEstimate calculates correct estimate for frozen foods`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 400,
            time = 30,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(400) // 400 - 0 (no reduction)
        assertThat(estimate.estimatedTime).isEqualTo(15)         // 30 * 0.5
        assertThat(estimate.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }

    @Test
    fun `getQuickEstimate calculates correct estimate for fresh vegetables`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 375,
            time = 40,
            category = FoodCategory.FRESH_VEGETABLES,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(350) // 375 - 25
        assertThat(estimate.estimatedTime).isEqualTo(32)         // 40 * 0.80
        assertThat(estimate.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }

    @Test
    fun `getQuickEstimate calculates correct estimate for raw meats`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 425,
            time = 60,
            category = FoodCategory.RAW_MEATS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(400) // 425 - 25
        assertThat(estimate.estimatedTime).isEqualTo(48)         // 60 * 0.80
        assertThat(estimate.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }

    @Test
    fun `getQuickEstimate calculates correct estimate for ready meals`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 350,
            time = 25,
            category = FoodCategory.READY_MEALS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(325) // 350 - 25
        assertThat(estimate.estimatedTime).isEqualTo(18)         // 25 * 0.75
        assertThat(estimate.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }

    @Test
    fun `getQuickEstimate handles Celsius temperatures correctly`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 200,
            time = 30,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.CELSIUS
        )

        // Temperature reduction: 0°F = 0°C (no reduction for frozen foods)
        assertThat(estimate.estimatedTemperature).isEqualTo(200) // 200 - 0
        assertThat(estimate.estimatedTime).isEqualTo(15)         // 30 * 0.5
        assertThat(estimate.temperatureUnit).isEqualTo(TemperatureUnit.CELSIUS)
    }

    @Test
    fun `getQuickEstimate handles edge case minimum values`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 200,
            time = 1,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(200) // 200 - 0 (no reduction)
        assertThat(estimate.estimatedTime).isEqualTo(0)          // 1 * 0.5 = 0.5 -> 0
    }

    @Test
    fun `getQuickEstimate handles edge case maximum values`() {
        val estimate = useCase.getQuickEstimate(
            temperature = 500,
            time = 300,
            category = FoodCategory.FROZEN_FOODS,
            unit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(estimate.estimatedTemperature).isEqualTo(500) // 500 - 0 (no reduction)
        assertThat(estimate.estimatedTime).isEqualTo(150)        // 300 * 0.5
    }

    // MARK: - ConversionEstimate Tests

    @Test
    fun `ConversionEstimate getFormattedTemperature includes unit symbol`() {
        val fahrenheitEstimate = ConversionEstimate(350, 20, TemperatureUnit.FAHRENHEIT)
        val celsiusEstimate = ConversionEstimate(175, 20, TemperatureUnit.CELSIUS)

        assertThat(fahrenheitEstimate.getFormattedTemperature()).isEqualTo("350°F")
        assertThat(celsiusEstimate.getFormattedTemperature()).isEqualTo("175°C")
    }

    @Test
    fun `ConversionEstimate getFormattedTime includes unit`() {
        val estimate = ConversionEstimate(350, 25, TemperatureUnit.FAHRENHEIT)

        assertThat(estimate.getFormattedTime()).isEqualTo("25 min")
    }

    // MARK: - ConversionException Tests

    @Test
    fun `ConversionException can be created with message only`() {
        val message = "Test conversion error"
        val exception = ConversionException(message)

        assertThat(exception.message).isEqualTo(message)
        assertThat(exception.cause).isNull()
    }

    @Test
    fun `ConversionException can be created with message and cause`() {
        val message = "Test conversion error"
        val cause = RuntimeException("Root cause")
        val exception = ConversionException(message, cause)

        assertThat(exception.message).isEqualTo(message)
        assertThat(exception.cause).isEqualTo(cause)
    }

    // MARK: - Integration-Style Tests

    @Test
    fun `execute handles complete conversion flow with real validation`() = runTest {
        val realValidator = ConversionValidator()
        val realUseCase = ConvertToAirFryerUseCase(mockRepository, realValidator)
        val input = TestData.Common.input375F25min

        val result = realUseCase.execute(input)

        assertThat(result.isSuccess).isTrue()
        coVerify { mockRepository.convertToAirFryer(input) }
    }

    @Test
    fun `execute handles complete validation failure with real validator`() = runTest {
        val realValidator = ConversionValidator()
        val realUseCase = ConvertToAirFryerUseCase(mockRepository, realValidator)
        val invalidInput = TestData.Invalid.temperatureTooLowF

        val result = realUseCase.execute(invalidInput)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ConversionException::class.java)
        coVerify(exactly = 0) { mockRepository.convertToAirFryer(any()) }
    }
}