package com.aircalc.converter

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumented tests.
 *
 * This runner replaces the default Application with HiltTestApplication,
 * which is required for @HiltAndroidTest to work properly.
 *
 * Configure in app/build.gradle:
 * ```
 * defaultConfig {
 *     testInstrumentationRunner "com.aircalc.converter.HiltTestRunner"
 * }
 * ```
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
