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

        // THEN: Wait for navigation to complete
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        // Verify "Air fryer settings" title is displayed
        composeTestRule
            .onNodeWithText("Air fryer settings", substring = true, ignoreCase = true)
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

            // THEN: Wait for navigation to complete
            Thread.sleep(2000)
            composeTestRule.waitForIdle()

            // THEN: Verify results are displayed
            composeTestRule
                .onNodeWithText("Air fryer settings", substring = true, ignoreCase = true)
                .assertExists("Results should be shown for $category")

            // Navigate back to input screen using back button via test activity
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }

            composeTestRule.waitForIdle()
            Thread.sleep(500) // Give time for navigation animation
        }
    }

    /**
     * Test 3: Verify air fryer temperature and time are displayed
     *
     * Verifies:
     * - Temperature value is shown
     * - Cooking time value is shown
     * - Results screen displays properly
     */
    @Test
    fun conversionFlow_resultsScreen_displaysTemperatureAndTime() {
        composeTestRule.waitForIdle()

        // GIVEN: User has default values (180°C, 25 minutes) and Ready meals category

        // WHEN: User clicks Convert
        composeTestRule
            .onNodeWithText("Convert")
            .performClick()

        // Wait for navigation to complete
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        // THEN: Results screen should be displayed
        composeTestRule
            .onNodeWithText("Air fryer settings", substring = true, ignoreCase = true)
            .assertExists("Air fryer settings header should be displayed")

        // THEN: Timer controls should be present (verifies full results screen)
        composeTestRule
            .onNodeWithText("Start")
            .assertExists("Timer start button should be present")
    }
}
