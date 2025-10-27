package com.aircalc.converter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for the conversion flow.
 *
 * This test verifies that users can:
 * 1. Select a food category
 * 2. Convert oven settings to air fryer settings
 * 3. See the results displayed correctly
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ConversionFlowTest {

    /**
     * Hilt rule must be first to inject dependencies before the activity is created
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Compose test rule creates the MainActivity and provides access to UI components
     */
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * Test 1: Complete conversion flow from input to results
     *
     * Verifies:
     * - User can select a food category
     * - User can click Convert button
     * - Results screen appears with air fryer settings
     * - Temperature and cooking time are displayed
     */
    @Test
    fun conversionFlow_fromInputToResults_displaysAirFryerSettings() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // GIVEN: User is on the input screen with default values (180°C, 25 minutes)
        // The app defaults to "Ready meals" category

        // WHEN: User clicks the "Frozen" food category
        composeTestRule
            .onNodeWithText("Frozen")
            .assertExists("Frozen food category should be visible")
            .performClick()

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // WHEN: User clicks Convert button
        composeTestRule
            .onNodeWithText("Convert")
            .assertExists("Convert button should be visible")
            .performClick()

        // THEN: Results screen should appear
        composeTestRule.waitForIdle()

        // Verify "Air fryer settings" title is displayed
        composeTestRule
            .onNodeWithText("Air fryer settings")
            .assertExists("Results screen should display 'Air fryer settings' title")

        // Verify timer controls are visible (indicates we're on the results screen)
        composeTestRule
            .onNodeWithText("Start")
            .assertExists("Start button should be visible on results screen")

        // Verify cooking tips section exists
        composeTestRule
            .onNodeWithText("Cooking tips:")
            .assertExists("Cooking tips should be visible on results screen")
    }

    /**
     * Test 2: Verify different food categories can be selected
     *
     * Verifies:
     * - All food category buttons are accessible
     * - Category selection updates the UI
     * - Conversion works with different categories
     */
    @Test
    fun conversionFlow_differentCategories_allCategoriesWork() {
        composeTestRule.waitForIdle()

        // Test each food category
        val categories = listOf("Ready meals", "Raw meat", "Veg", "Frozen")

        categories.forEach { category ->
            // GIVEN: User selects a specific food category
            composeTestRule
                .onNodeWithText(category)
                .assertExists("$category category should be visible")
                .performClick()

            composeTestRule.waitForIdle()

            // WHEN: User clicks Convert
            composeTestRule
                .onNodeWithText("Convert")
                .performClick()

            // THEN: Results should be displayed
            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithText("Air fryer settings")
                .assertExists("Results should be shown for $category")

            // Navigate back to input screen for next category
            composeTestRule
                .onNodeWithText("AirCalc")
                .performClick()

            composeTestRule.waitForIdle()
        }
    }

    /**
     * Test 3: Verify air fryer temperature and time are displayed
     *
     * Verifies:
     * - Temperature value is shown
     * - Cooking time value is shown
     * - Values use correct content descriptions for accessibility
     */
    @Test
    fun conversionFlow_resultsScreen_displaysTemperatureAndTime() {
        composeTestRule.waitForIdle()

        // GIVEN: User has default values (180°C, 25 minutes) and Ready meals category

        // WHEN: User clicks Convert
        composeTestRule
            .onNodeWithText("Convert")
            .performClick()

        composeTestRule.waitForIdle()

        // THEN: Results should display air fryer temperature
        // For Ready meals at 180°C: should reduce to 155°C (180 - 25°F = 155°C)
        composeTestRule
            .onNode(hasContentDescription("Air fryer temperature", substring = true))
            .assertExists("Air fryer temperature should be displayed with content description")

        // THEN: Results should display air fryer cooking time
        // For Ready meals at 25 minutes: should be 18-19 minutes (75% of 25 = 18.75)
        composeTestRule
            .onNode(hasContentDescription("Air fryer cooking time", substring = true))
            .assertExists("Air fryer cooking time should be displayed with content description")
    }
}
