package com.aircalc.converter

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aircalc.converter.presentation.screen.ConversionResultsScreen
import com.aircalc.converter.ui.theme.AirCalcTheme
import com.aircalc.converter.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen for Android 12+
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // HONOR/MagicOS FIX: Aggressive multi-layered approach
        val isDarkMode = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        // 1. Set FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS explicitly
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (isDarkMode) {
            // 2. Use modern API with explicit dark scrim
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    scrim = android.graphics.Color.BLACK
                ),
                navigationBarStyle = SystemBarStyle.dark(
                    scrim = android.graphics.Color.BLACK
                )
            )

            // 3. HONOR/MagicOS workaround: Force-set colors for devices that don't respect enableEdgeToEdge
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.BLACK
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.BLACK

            // 4. Post to decorView to apply after view initialization
            window.decorView.post {
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.BLACK
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.BLACK

                androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        } else {
            // Light mode
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT
                )
            )
        }

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirFryerConverterApp() {
    val navController = rememberNavController()
    var ovenTemp by remember { mutableStateOf(180) }
    var cookingTime by remember { mutableStateOf(25) }
    var selectedCategory by remember { mutableStateOf(FoodCategory.REFRIGERATED_READY_MEALS) }
    var temperatureUnit by remember { mutableStateOf(TemperatureUnit.CELSIUS) }
    var conversionResult by remember { mutableStateOf<com.aircalc.converter.domain.model.ConversionResult?>(null) }
    var isConverting by remember { mutableStateOf(false) }
    val timerState = rememberTimerState()

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val scope = rememberCoroutineScope()

    // Set up timer finished callback to play alarm and vibrate
    LaunchedEffect(Unit) {
        timerState.onTimerFinished = {
            scope.launch {
                try {
                    // Vibrate
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Pattern: vibrate for 500ms, pause 200ms, vibrate 500ms, pause 200ms, vibrate 500ms
                        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                    }

                    // Play alarm sound using MediaPlayer for better volume control
                    try {
                        mediaPlayer?.release() // Release any existing player
                        mediaPlayer = MediaPlayer().apply {
                            // Use default alarm sound
                            setDataSource(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)

                            // Set audio attributes to use alarm stream
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                setAudioStreamType(AudioManager.STREAM_ALARM)
                            }

                            // Set volume to maximum
                            setVolume(1.0f, 1.0f)

                            // Prepare and play
                            prepare()
                            start()

                            // Set completion listener to release resources
                            setOnCompletionListener { mp ->
                                mp.release()
                            }
                        }

                        // Stop after 5 seconds if still playing
                        kotlinx.coroutines.delay(5000)
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                mp.stop()
                            }
                            mp.release()
                        }
                        mediaPlayer = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback: just continue without sound
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Clean up media player when app is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedTimer(timerState)

    NavHost(
        navController = navController,
        startDestination = "input"
    ) {
        composable("input") {
            ConversionInputScreen(
                ovenTemp = ovenTemp,
                cookingTime = cookingTime,
                selectedCategory = selectedCategory,
                temperatureUnit = temperatureUnit,
                isConverting = isConverting,
                onTemperatureChange = { ovenTemp = it },
                onTimeChange = { cookingTime = it },
                onCategoryChange = { selectedCategory = it },
                onTemperatureUnitChange = { temperatureUnit = it },
                onConvert = { result ->
                    conversionResult = result
                    isConverting = false
                    navController.navigate("results")
                },
                onConvertingStateChange = { isConverting = it }
            )
        }

        composable("results") {
            conversionResult?.let { result ->
                // Initialize timer with cooking time when screen loads
                LaunchedEffect(result.airFryerTimeMinutes) {
                    timerState.resetTimer(result.airFryerTimeMinutes)
                }

                ConversionResultsScreen(
                    conversionResult = result,
                    timerState = timerState,
                    onStartTimer = {
                        timerState.startTimer()
                    },
                    onPauseTimer = { timerState.pauseTimer() },
                    onResumeTimer = { timerState.startTimer() },
                    onResetTimer = { timerState.resetTimer(result.airFryerTimeMinutes) },
                    onNavigateBack = { navController.popBackStack() }
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
    onConvert: (com.aircalc.converter.domain.model.ConversionResult) -> Unit,
    onConvertingStateChange: (Boolean) -> Unit
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

                    onConvertingStateChange(true)
                    // Convert UI types to domain types for conversion
                    val domainTempUnit = when (temperatureUnit) {
                        TemperatureUnit.FAHRENHEIT -> com.aircalc.converter.domain.model.TemperatureUnit.FAHRENHEIT
                        TemperatureUnit.CELSIUS -> com.aircalc.converter.domain.model.TemperatureUnit.CELSIUS
                    }
                    val domainCategory = when (selectedCategory) {
                        FoodCategory.FROZEN_FOODS -> com.aircalc.converter.domain.model.FoodCategory.FROZEN_FOODS
                        FoodCategory.FRESH_VEGETABLES -> com.aircalc.converter.domain.model.FoodCategory.FRESH_VEGETABLES
                        FoodCategory.MEATS_RAW -> com.aircalc.converter.domain.model.FoodCategory.RAW_MEATS
                        FoodCategory.REFRIGERATED_READY_MEALS -> com.aircalc.converter.domain.model.FoodCategory.READY_MEALS
                    }

                    // Calculate actual temperature reduction based on unit
                    val tempReduction = domainTempUnit.convertTempReduction(
                        domainCategory.tempReductionFahrenheit
                    )
                    val airFryerTemp = ovenTemp - tempReduction
                    val airFryerTime = (cookingTime * domainCategory.timeMultiplier).toInt()

                    // Create conversion result with domain types
                    val result = com.aircalc.converter.domain.model.ConversionResult(
                        originalTemperature = ovenTemp,
                        originalTime = cookingTime,
                        airFryerTemperature = airFryerTemp,
                        airFryerTimeMinutes = airFryerTime,
                        temperatureUnit = domainTempUnit,
                        foodCategory = domainCategory,
                        cookingTip = domainCategory.cookingTip,
                        temperatureReduction = tempReduction,
                        timeReduction = cookingTime - airFryerTime
                    )
                    onConvert(result)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        content()
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
fun UnitToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) PureWhite else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = PureBlack
        )
    }
}

@Composable
fun ModernInputCard(
    title: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
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

@Composable
fun ModernConvertButton(
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
            containerColor = PrimaryRed,
            contentColor = PureWhite,
            disabledContainerColor = MediumGray,
            disabledContentColor = PureWhite
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PureWhite
            )
        } else {
            Text(
                text = stringResource(R.string.convert_to_air_fryer),
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

@Composable
fun TemperatureInputSection(
    temperature: Int,
    onTemperatureChange: (Int) -> Unit,
    unit: TemperatureUnit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val minTemp = if (unit == TemperatureUnit.CELSIUS) 120 else 250
    val maxTemp = if (unit == TemperatureUnit.CELSIUS) 250 else 480
    val increment = 5

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else PureWhite
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.tertiaryContainer else MediumGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Far left: Minus button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .holdToRepeat(
                        onIncrement = {
                            focusManager.clearFocus()
                            if (temperature > minTemp) {
                                onTemperatureChange(temperature - increment)
                            }
                        },
                        hapticFeedback = hapticFeedback
                    )
                    .semantics { contentDescription = "Decrease temperature" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            // Center section: Icon + Title + Value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_temperature),
                        contentDescription = null,
                        tint = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.temperature),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$temperature${unit.symbol}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                    modifier = Modifier.clickable {
                        // TODO: Show keyboard input dialog
                    }
                )
            }

            // Far right: Plus button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .holdToRepeat(
                        onIncrement = {
                            focusManager.clearFocus()
                            if (temperature < maxTemp) {
                                onTemperatureChange(temperature + increment)
                            }
                        },
                        hapticFeedback = hapticFeedback
                    )
                    .semantics { contentDescription = "Increase temperature" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun TimeInputSection(
    time: Int,
    onTimeChange: (Int) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val minTime = 1
    val maxTime = 180
    val increment = 1

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else PureWhite
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.tertiaryContainer else MediumGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Far left: Minus button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .holdToRepeat(
                        onIncrement = {
                            focusManager.clearFocus()
                            if (time > minTime) {
                                onTimeChange(maxOf(minTime, time - increment))
                            }
                        },
                        hapticFeedback = hapticFeedback
                    )
                    .semantics { contentDescription = "Decrease cooking time" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            // Center section: Icon + Title + Value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_time),
                        contentDescription = null,
                        tint = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.cooking_time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${time}m",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                    modifier = Modifier.clickable {
                        // TODO: Show keyboard input dialog
                    }
                )
            }

            // Far right: Plus button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .holdToRepeat(
                        onIncrement = {
                            focusManager.clearFocus()
                            if (time < maxTime) {
                                onTimeChange(minOf(maxTime, time + increment))
                            }
                        },
                        hapticFeedback = hapticFeedback
                    )
                    .semantics { contentDescription = "Increase cooking time" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun FoodCategoryGrid(
    selectedCategory: FoodCategory,
    onCategorySelected: (FoodCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter out baked goods to show only 4 categories in 2x2 grid
    val categories = listOf(
        FoodCategory.REFRIGERATED_READY_MEALS,
        FoodCategory.MEATS_RAW,
        FoodCategory.FRESH_VEGETABLES,
        FoodCategory.FROZEN_FOODS
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridFoodCategoryCard(
                category = categories[0],
                isSelected = categories[0] == selectedCategory,
                onSelected = { onCategorySelected(categories[0]) },
                modifier = Modifier.weight(1f)
            )
            GridFoodCategoryCard(
                category = categories[1],
                isSelected = categories[1] == selectedCategory,
                onSelected = { onCategorySelected(categories[1]) },
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridFoodCategoryCard(
                category = categories[2],
                isSelected = categories[2] == selectedCategory,
                onSelected = { onCategorySelected(categories[2]) },
                modifier = Modifier.weight(1f)
            )
            GridFoodCategoryCard(
                category = categories[3],
                isSelected = categories[3] == selectedCategory,
                onSelected = { onCategorySelected(categories[3]) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GridFoodCategoryCard(
    category: FoodCategory,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isDark) MaterialTheme.colorScheme.primary else CreamBackground
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) {
                if (isDark) MaterialTheme.colorScheme.primary else PrimaryRed
            } else {
                if (isDark) MaterialTheme.colorScheme.tertiaryContainer else MediumGray
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = getCategoryIconResource(category)),
                contentDescription = null,
                tint = if (isDark) {
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    PrimaryRed
                },
                modifier = Modifier
                    .size(36.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = getCategoryDisplayName(category),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isDark) {
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    PureBlack
                },
                maxLines = 2,
                lineHeight = 16.sp
            )
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

@Composable
fun FoodCategorySection(
    selectedCategory: FoodCategory,
    onCategorySelected: (FoodCategory) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(FoodCategory.values()) { category ->
                ModernFoodCategoryCard(
                    category = category,
                    isSelected = category == selectedCategory,
                    onSelected = { onCategorySelected(category) }
                )
            }
        }

        // Show description for selected category
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedCategory.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = selectedCategory.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ModernFoodCategoryCard(
    category: FoodCategory,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 90.dp)
            .background(
                color = if (isSelected) PrimaryRed else LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelected() }
            .semantics {
                contentDescription = "Select ${category.displayName} food category"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isSelected) PureWhite else PureBlack,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun FoodCategoryCard(
    category: FoodCategory,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    BorderCard(
        modifier = Modifier
            .scale(scale)
            .size(width = 120.dp, height = 100.dp)
            .clickable { onSelected() }
            .semantics {
                contentDescription = "Select ${category.displayName} food category"
            },
        backgroundColor = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        borderColor = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun ConvertButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics { contentDescription = "Convert oven settings to air fryer settings" },
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "🔄 Convert to Air Fryer",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ResultsSection(
    result: ConversionResult,
    onStartTimer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "✨ Air Fryer Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultCard(
                    value = "${result.airFryerTemp}${result.temperatureUnit.symbol}",
                    label = "Temperature",
                    icon = "🌡️"
                )

                ResultCard(
                    value = "${result.airFryerTimeMinutes}",
                    label = "Minutes",
                    icon = "⏰"
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 ${result.tip}",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onStartTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "⏰ Start ${result.airFryerTimeMinutes} Min Timer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ResultCard(
    value: String,
    label: String,
    icon: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.size(width = 140.dp, height = 100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TimerSection(
    timerState: TimerState,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (timerState.isFinished)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (timerState.isFinished) "🔔 Time's Up!" else "⏱️ Cooking Timer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = timerState.timeLeftFormatted,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = if (timerState.isFinished)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.semantics {
                    contentDescription = "Timer: ${timerState.timeLeftFormatted}"
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!timerState.isFinished) {
                    Button(
                        onClick = {
                            if (timerState.isRunning) timerState.pauseTimer()
                            else timerState.startTimer()
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = if (timerState.isRunning) "⏸️ Pause" else "▶️ Resume",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "🔄 Reset",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AirFryerConverterPreview() {
    AirCalcTheme {
        AirFryerConverterApp()
    }
}