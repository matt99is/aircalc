package com.aircalc.converter

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// import androidx.compose.material.icons.filled.Remove // Not available
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aircalc.converter.presentation.screen.ConversionResultsScreen
import com.aircalc.converter.presentation.screen.DisclaimerScreen
import com.aircalc.converter.presentation.viewmodel.AirFryerViewModel
import com.aircalc.converter.presentation.components.BorderCard
import com.aircalc.converter.presentation.components.TemperatureInputSection
import com.aircalc.converter.presentation.components.TimeInputSection
import com.aircalc.converter.presentation.components.FoodCategoryGrid
import com.aircalc.converter.ui.theme.AirCalcTheme
import com.aircalc.converter.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen for Android 12+
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Lock orientation to portrait on phones, allow rotation on tablets
        val screenLayout = resources.configuration.screenLayout
        val screenSize = screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize < android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // Phone - lock to portrait
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        // Tablets (LARGE or XLARGE) - allow rotation (default behavior)

        // Apply theme-specific window configuration
        WindowThemeManager.applyTheme(this)

        setContent {
            AirCalcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AirFryerConverterApp()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        // Re-apply window theme when configuration changes (e.g., dark/light mode switch)
        WindowThemeManager.applyTheme(this)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirFryerConverterApp(
    viewModel: AirFryerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsState()

    // Determine start destination based on disclaimer acceptance
    val startDestination = if (isDisclaimerAccepted) "input" else "disclaimer"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("disclaimer") {
            DisclaimerScreen(
                onAccept = {
                    viewModel.acceptDisclaimer()
                    // Navigate to input screen after acceptance
                    navController.navigate("input") {
                        // Remove disclaimer from back stack
                        popUpTo("disclaimer") { inclusive = true }
                    }
                }
            )
        }

        composable("input") {
            // Map domain FoodCategory to UI FoodCategory for backward compatibility
            val uiFoodCategory = uiState.selectedCategory?.let { domainCat ->
                when (domainCat.id) {
                    "frozen_foods" -> FoodCategory.FROZEN_FOODS
                    "fresh_vegetables" -> FoodCategory.FRESH_VEGETABLES
                    "raw_meats" -> FoodCategory.MEATS_RAW
                    "ready_meals" -> FoodCategory.REFRIGERATED_READY_MEALS
                    else -> FoodCategory.REFRIGERATED_READY_MEALS
                }
            } ?: FoodCategory.REFRIGERATED_READY_MEALS

            val uiTempUnit = when (uiState.temperatureUnit) {
                com.aircalc.converter.domain.model.TemperatureUnit.FAHRENHEIT -> TemperatureUnit.FAHRENHEIT
                com.aircalc.converter.domain.model.TemperatureUnit.CELSIUS -> TemperatureUnit.CELSIUS
            }

            ConversionInputScreen(
                ovenTemp = uiState.ovenTemperature,
                cookingTime = uiState.cookingTime,
                selectedCategory = uiFoodCategory,
                temperatureUnit = uiTempUnit,
                isConverting = uiState.isConverting,
                onTemperatureChange = { viewModel.updateTemperature(it) },
                onTimeChange = { viewModel.updateCookingTime(it) },
                onCategoryChange = { uiCat ->
                    // Map UI FoodCategory to domain FoodCategory
                    val domainCat = when (uiCat) {
                        FoodCategory.FROZEN_FOODS -> com.aircalc.converter.domain.model.FoodCategory.FROZEN_FOODS
                        FoodCategory.FRESH_VEGETABLES -> com.aircalc.converter.domain.model.FoodCategory.FRESH_VEGETABLES
                        FoodCategory.MEATS_RAW -> com.aircalc.converter.domain.model.FoodCategory.RAW_MEATS
                        FoodCategory.REFRIGERATED_READY_MEALS -> com.aircalc.converter.domain.model.FoodCategory.READY_MEALS
                    }
                    viewModel.updateSelectedCategory(domainCat)
                },
                onTemperatureUnitChange = { uiUnit ->
                    val domainUnit = when (uiUnit) {
                        TemperatureUnit.FAHRENHEIT -> com.aircalc.converter.domain.model.TemperatureUnit.FAHRENHEIT
                        TemperatureUnit.CELSIUS -> com.aircalc.converter.domain.model.TemperatureUnit.CELSIUS
                    }
                    viewModel.updateTemperatureUnit(domainUnit)
                },
                onConvert = { _ ->
                    viewModel.convertToAirFryer()
                }
            )

            // Navigate when conversion completes
            LaunchedEffect(uiState.conversionResult) {
                if (uiState.conversionResult != null && !uiState.isConverting) {
                    // Stop any existing timer before navigating
                    viewModel.resetTimer()
                    delay(100) // Wait for timer to stop

                    navController.navigate("results")
                }
            }
        }

        composable("results") {
            uiState.conversionResult?.let { result ->
                // Initialize timer when entering results screen
                // Key on result to ensure timer resets for each new conversion
                LaunchedEffect(result) {
                    // Timer was already reset before navigation
                    // Just start the new timer
                    viewModel.startTimer(result.airFryerTimeMinutes)
                    delay(100) // Wait for timer to initialize
                    viewModel.pauseTimer()
                }

                // Convert timer state to legacy UI timer state format
                val legacyTimerState = remember(timerState) {
                    object : TimerState() {
                        override val timeLeftSeconds: Int get() = timerState.remainingSeconds
                        override val isRunning: Boolean get() = timerState.isRunning
                        override val isFinished: Boolean get() = timerState.isFinished
                        override val timeLeftFormatted: String get() = timerState.formattedTime

                        override fun startTimer() {
                            viewModel.startTimer(result.airFryerTimeMinutes)
                        }

                        override fun pauseTimer() {
                            viewModel.pauseTimer()
                        }

                        override fun resetTimer(minutes: Int) {
                            viewModel.resetTimer()
                        }
                    }
                }

                val resetScope = rememberCoroutineScope()

                ConversionResultsScreen(
                    conversionResult = result,
                    timerState = legacyTimerState,
                    onStartTimer = {
                        // Start or resume the timer
                        if (timerState.remainingSeconds == 0 || timerState.isFinished) {
                            viewModel.startTimer(result.airFryerTimeMinutes)
                        } else if (!timerState.isRunning) {
                            viewModel.resumeTimer()
                        }
                    },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onResumeTimer = { viewModel.resumeTimer() },
                    onResetTimer = {
                        resetScope.launch {
                            // Start fresh timer - this replaces the old one
                            viewModel.startTimer(result.airFryerTimeMinutes)
                            delay(50) // Wait for timer to initialize
                            viewModel.pauseTimer()
                        }
                    },
                    onNavigateBack = {
                        viewModel.clearResult()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionInputScreen(
    ovenTemp: Int,
    cookingTime: Int,
    selectedCategory: FoodCategory,
    temperatureUnit: TemperatureUnit,
    isConverting: Boolean,
    onTemperatureChange: (Int) -> Unit,
    onTimeChange: (Int) -> Unit,
    onCategoryChange: (FoodCategory) -> Unit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onConvert: (com.aircalc.converter.domain.model.ConversionResult?) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Header Component (64dp height, responsive width) - Hidden behind feature flag
        val showHeader = false // Feature flag for header
        if (showHeader) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // App title
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Add spacing when header is hidden
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Temperature Unit Toggle
        TemperatureUnitToggle(
            unit = temperatureUnit,
            onUnitChange = { newUnit ->
                onTemperatureUnitChange(newUnit)
                // Convert temperature when unit changes
                if (newUnit == TemperatureUnit.FAHRENHEIT && temperatureUnit == TemperatureUnit.CELSIUS) {
                    onTemperatureChange(350) // Default F temp
                } else if (newUnit == TemperatureUnit.CELSIUS && temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                    onTemperatureChange(180) // Default C temp
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Temperature Section
            TemperatureInputSection(
                temperature = ovenTemp,
                onTemperatureChange = onTemperatureChange,
                unit = temperatureUnit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Section
            TimeInputSection(
                time = cookingTime,
                onTimeChange = onTimeChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Food Category Section (2x2 Grid)
            FoodCategoryGrid(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategoryChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Food Category Description
            val isDarkDesc = androidx.compose.foundation.isSystemInDarkTheme()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = if (isDarkDesc) MaterialTheme.colorScheme.tertiaryContainer else LightGray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = getCategoryDescription(selectedCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkDesc) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Convert Button
            GreenConvertButton(
                isEnabled = ovenTemp > 0 && cookingTime > 0,
                isLoading = isConverting,
                onClick = {
                    // Clear focus to exit any editing mode
                    focusManager.clearFocus()

                    // Trigger conversion via callback (ViewModel handles actual conversion)
                    onConvert(null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TemperatureUnitToggle(
    unit: TemperatureUnit,
    onUnitChange: (TemperatureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val animatedOffset by animateFloatAsState(
        targetValue = if (unit == TemperatureUnit.CELSIUS) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "toggle_animation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else LightGray,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(4.dp)
    ) {
        val density = LocalDensity.current
        var dragOffset by remember { mutableStateOf(0f) }
        val halfWidthPx = with(density) { maxWidth.toPx() } / 2f

        val draggableState = rememberDraggableState { delta ->
            dragOffset += delta
        }
        val maxWidthPx = maxWidth
        val halfWidth = maxWidthPx / 2

        // Animated sliding background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(halfWidth)
                .offset {
                    IntOffset(
                        x = (animatedOffset * halfWidth.toPx()).toInt(),
                        y = 0
                    )
                }
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(28.dp)
                )
        )

        // Button row with drag functionality
        Row(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { _ ->
                        val threshold = halfWidthPx / 2f

                        when {
                            dragOffset > threshold.toFloat() && unit == TemperatureUnit.CELSIUS -> {
                                onUnitChange(TemperatureUnit.FAHRENHEIT)
                            }
                            dragOffset < (-threshold).toFloat() && unit == TemperatureUnit.FAHRENHEIT -> {
                                onUnitChange(TemperatureUnit.CELSIUS)
                            }
                        }
                        dragOffset = 0f
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Celsius button (left)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onUnitChange(TemperatureUnit.CELSIUS)
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Fahrenheit button (right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onUnitChange(TemperatureUnit.FAHRENHEIT)
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "°F",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun ModernInputCardWithIcon(
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp,
                    color = PrimaryRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PureBlack
                )
            }
            content()
        }
    }
}

@Composable
fun GreenConvertButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PureWhite
            )
        } else {
            Text(
                text = stringResource(R.string.convert),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/**
 * Modifier that enables hold-to-repeat functionality with 3-phase acceleration.
 * - Tap: Single increment
 * - Hold: Continuous increments with acceleration (slow → medium → fast)
 * - Haptic feedback pulses on each increment
 */
@Composable
fun Modifier.holdToRepeat(
    onIncrement: () -> Unit,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
): Modifier {
    val scope = rememberCoroutineScope()
    // Remember the latest callbacks to avoid stale closures
    val currentOnIncrement by rememberUpdatedState(onIncrement)
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)

    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)

            val repeatJob = scope.launch(Dispatchers.Default) {
                // Initial increment
                withContext(Dispatchers.Main) {
                    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnIncrement()
                }

                delay(300) // Initial delay

                // Phase 1: Slow (8 increments at 200ms intervals)
                repeat(8) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(200)
                }

                // Phase 2: Medium (10 increments at 150ms intervals)
                repeat(10) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(150)
                }

                // Phase 3: Fast (continuous at 80ms intervals)
                while (true) {
                    withContext(Dispatchers.Main) {
                        currentHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnIncrement()
                    }
                    delay(80)
                }
            }

            // Wait for release
            do {
                val event = awaitPointerEvent()
            } while (event.changes.any { it.pressed })

            repeatJob.cancel()
        }
    }
}

// Helper functions to get category specific data
private fun getCategoryIcon(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> "🥘"
        FoodCategory.MEATS_RAW -> "🥩"
        FoodCategory.FRESH_VEGETABLES -> "🥕"
        FoodCategory.FROZEN_FOODS -> "❄️"
    }
}

private fun getCategoryIconResource(category: FoodCategory): Int {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> R.drawable.ic_ready_meals
        FoodCategory.MEATS_RAW -> R.drawable.ic_raw_meat
        FoodCategory.FRESH_VEGETABLES -> R.drawable.ic_veg
        FoodCategory.FROZEN_FOODS -> R.drawable.ic_frozen
    }
}

@Composable
private fun getCategoryDescription(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> stringResource(R.string.refrigerated_ready_meals_desc)
        FoodCategory.MEATS_RAW -> stringResource(R.string.meats_raw_desc)
        FoodCategory.FRESH_VEGETABLES -> stringResource(R.string.fresh_vegetables_desc)
        FoodCategory.FROZEN_FOODS -> stringResource(R.string.frozen_foods_desc)
    }
}

@Composable
private fun getCategoryDisplayName(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> stringResource(R.string.ready_meals)
        FoodCategory.MEATS_RAW -> stringResource(R.string.raw_meat)
        FoodCategory.FRESH_VEGETABLES -> stringResource(R.string.veg)
        FoodCategory.FROZEN_FOODS -> stringResource(R.string.frozen)
    }
}

@Preview(showBackground = true)
@Composable
fun AirFryerConverterPreview() {
    AirCalcTheme {
        AirFryerConverterApp()
    }
}