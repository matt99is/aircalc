package com.aircalc.converter.di

import android.content.Context
import com.aircalc.converter.data.datasource.ConversionDataSource
import com.aircalc.converter.data.repository.ConversionCache
import com.aircalc.converter.data.repository.ConversionRepositoryImpl
import com.aircalc.converter.domain.repository.ConversionRepository
import com.aircalc.converter.domain.usecase.ConversionValidator
import com.aircalc.converter.domain.usecase.ConvertToAirFryerUseCase
import com.aircalc.converter.presentation.timer.TimerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for dependency injection.
 * Demonstrates proper DI setup for clean architecture.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConversionDataSource(): ConversionDataSource {
        return ConversionDataSource()
    }

    @Provides
    @Singleton
    fun provideConversionCache(): ConversionCache {
        return ConversionCache()
    }

    @Provides
    @Singleton
    fun provideConversionRepository(
        dataSource: ConversionDataSource,
        cache: ConversionCache
    ): ConversionRepository {
        return ConversionRepositoryImpl(dataSource, cache)
    }

    @Provides
    @Singleton
    fun provideConversionValidator(): ConversionValidator {
        return ConversionValidator()
    }

    @Provides
    @Singleton
    fun provideConvertToAirFryerUseCase(
        repository: ConversionRepository,
        validator: ConversionValidator
    ): ConvertToAirFryerUseCase {
        return ConvertToAirFryerUseCase(repository, validator)
    }

    @Provides
    @Singleton
    fun provideTimerManager(
        @ApplicationContext context: Context
    ): TimerManager {
        return TimerManager(context)
    }
}