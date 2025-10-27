# AirCalc - Testing Guide

This document explains how to run the instrumented UI tests for AirCalc.

## Test Overview

We have **3 basic instrumented UI tests** that verify core functionality:

### 1. ConversionFlowTest.kt
Tests the conversion flow from input to results:
- ✅ User can select food categories
- ✅ User can convert oven settings to air fryer settings
- ✅ Results display correctly with temperature and cooking time
- ✅ All food categories work correctly

**Tests included:**
- `conversionFlow_fromInputToResults_displaysAirFryerSettings()`
- `conversionFlow_differentCategories_allCategoriesWork()`
- `conversionFlow_resultsScreen_displaysTemperatureAndTime()`

### 2. TimerFlowTest.kt
Tests the cooking timer functionality:
- ✅ Timer can be started
- ✅ Timer can be paused and resumed
- ✅ Timer can be reset
- ✅ Timer counts down correctly
- ✅ All timer controls are accessible

**Tests included:**
- `timerFlow_startTimer_timerBegins()`
- `timerFlow_pauseAndResumeTimer_stateChangesCorrectly()`
- `timerFlow_resetTimer_timerResetsToInitialState()`
- `timerFlow_timerControls_allButtonsAccessible()`
- `timerFlow_timerCountdown_timeDecreases()`

## Prerequisites

- Android Studio installed
- Android device or emulator running
- USB debugging enabled (for physical devices)

## Running the Tests

### Option 1: Android Studio GUI

1. **Open the project** in Android Studio
2. **Connect a device** or **start an emulator**
3. **Navigate to** `app/src/androidTest/java/com/aircalc/converter/`
4. **Right-click** on the test file or test class
5. **Select** "Run 'ConversionFlowTest'" or "Run 'TimerFlowTest'"
6. **View results** in the Run window

### Option 2: Command Line (All Tests)

Run all instrumented tests:
```bash
./gradlew connectedAndroidTest
```

Run tests with more verbose output:
```bash
./gradlew connectedAndroidTest --info
```

### Option 3: Command Line (Specific Test Class)

Run only ConversionFlowTest:
```bash
./gradlew connectedAndroidTest --tests "com.aircalc.converter.ConversionFlowTest"
```

Run only TimerFlowTest:
```bash
./gradlew connectedAndroidTest --tests "com.aircalc.converter.TimerFlowTest"
```

### Option 4: Command Line (Specific Test Method)

Run a single test method:
```bash
./gradlew connectedAndroidTest --tests "com.aircalc.converter.ConversionFlowTest.conversionFlow_fromInputToResults_displaysAirFryerSettings"
```

## Test Reports

After running tests, view the HTML report at:
```
app/build/reports/androidTests/connected/index.html
```

Open this file in a browser to see:
- Test results (pass/fail)
- Execution time
- Screenshots (if failures occur)
- Stack traces for failures

## Device/Emulator Requirements

### Minimum Requirements
- **API Level**: 24 (Android 7.0) or higher
- **Screen**: Any size (tests are resolution-independent)
- **Internet**: Not required (app is fully offline)

### Recommended Setup
- **API Level**: 33 or 34 (Android 13/14) - closest to target SDK 35
- **Device**: Pixel 5 or similar (standard screen size)
- **System Animation**: Can be enabled (tests handle waits properly)

### Optimizing Test Speed
To speed up tests, you can disable animations on the device:
1. Enable **Developer Options** on your device
2. Set these to **0.5x** or **off**:
   - Window animation scale
   - Transition animation scale
   - Animator duration scale

## Troubleshooting

### Test fails with "Element not found"
- **Cause**: UI not fully loaded
- **Solution**: Tests include `waitForIdle()` calls, but if issues persist, increase wait times

### Test fails with "Activity not found"
- **Cause**: Hilt dependency injection not set up
- **Solution**: Tests already include `@HiltAndroidTest` and `HiltAndroidRule`

### Test hangs or times out
- **Cause**: Device is too slow or animation is stuck
- **Solution**:
  - Use a faster emulator or physical device
  - Disable animations (see above)
  - Check if the app is frozen in the UI

### "No connected devices" error
- **Cause**: No device or emulator is running
- **Solution**:
  - Start an emulator: `emulator -avd <avd_name>`
  - Or connect a physical device with USB debugging

### Gradle sync issues
- **Cause**: Dependencies not downloaded
- **Solution**: Run `./gradlew build` first

## Test Architecture

### Technology Stack
- **Compose Testing**: `androidx.compose.ui:ui-test-junit4`
- **JUnit4**: Test runner
- **Hilt Testing**: `dagger.hilt.android.testing`
- **Espresso**: Core Android UI testing (transitive dependency)

### Test Strategy
- **Integration Tests**: Tests exercise real app behavior, not mocked
- **Happy Path Focus**: Tests verify main user flows work correctly
- **Accessibility**: Tests verify content descriptions are present
- **Reliability**: Tests use proper waits and synchronization

### Test Structure
```
app/src/androidTest/java/com/aircalc/converter/
├── ConversionFlowTest.kt    # Conversion functionality
└── TimerFlowTest.kt          # Timer functionality
```

## Adding More Tests

To add more tests:

1. **Create a new test file** in `app/src/androidTest/java/com/aircalc/converter/`
2. **Use the same structure**:
   ```kotlin
   @HiltAndroidTest
   @RunWith(AndroidJUnit4::class)
   class MyNewTest {
       @get:Rule(order = 0)
       val hiltRule = HiltAndroidRule(this)

       @get:Rule(order = 1)
       val composeTestRule = createAndroidComposeRule<MainActivity>()

       @Before
       fun setup() {
           hiltRule.inject()
       }

       @Test
       fun myTest() {
           // Test code
       }
   }
   ```
3. **Use Compose test finders**:
   - `onNodeWithText("text")` - Find by text
   - `onNodeWithContentDescription("desc")` - Find by accessibility label
   - `onNode(hasText("text") and hasContentDescription("desc"))` - Combine matchers

## CI/CD Integration

To run these tests in CI (e.g., GitHub Actions):

```yaml
- name: Run instrumented tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 33
    target: google_apis
    arch: x86_64
    script: ./gradlew connectedAndroidTest
```

## Next Steps

These basic tests cover the core flows. Consider adding:
- Edge case testing (invalid inputs, boundary values)
- Error state testing (what happens when things go wrong)
- Accessibility testing (screen reader compatibility)
- Performance testing (app startup time, conversion speed)
- Screenshot testing (visual regression)

## Questions?

If you encounter issues:
1. Check the test reports (HTML file mentioned above)
2. Run tests individually to isolate failures
3. Check device logs: `adb logcat | grep AirCalc`
4. Verify the app works manually on the device first

Happy testing! 🧪
