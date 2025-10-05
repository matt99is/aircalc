package com.aircalc.converter

import android.media.RingtoneManager
import android.media.Ringtone
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// import androidx.compose.material.icons.filled.Remove // Not available
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    var selectedCategory by remember { mutableStateOf(FoodCategory.FROZEN_FOODS) }
    var temperatureUnit by remember { mutableStateOf(TemperatureUnit.CELSIUS) }
    var conversionResult by remember { mutableStateOf<com.aircalc.converter.domain.model.ConversionResult?>(null) }
    var isConverting by remember { mutableStateOf(false) }
    val timerState = rememberTimerState()

    val context = LocalContext.current
    var ringtone by remember { mutableStateOf<Ringtone?>(null) }
    val scope = rememberCoroutineScope()

    // Set up timer finished callback to play alarm
    LaunchedEffect(Unit) {
        timerState.onTimerFinished = {
            scope.launch {
                try {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ringtone = RingtoneManager.getRingtone(context, notificationUri)
                    ringtone?.play()

                    // Stop after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    ringtone?.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Clean up ringtone when app is disposed
    DisposableEffect(Unit) {
        onDispose {
            ringtone?.stop()
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Component (64dp height, responsive width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // App title
            Text(
                text = "AirCalc",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PureBlack,
                textAlign = TextAlign.Center
            )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = getCategoryDescription(selectedCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureBlack.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Convert Button
            GreenConvertButton(
                isEnabled = ovenTemp > 0 && cookingTime > 0,
                isLoading = isConverting,
                onClick = {
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
                        FoodCategory.BAKED_GOODS -> com.aircalc.converter.domain.model.FoodCategory.BAKED_GOODS
                        FoodCategory.REFRIGERATED_READY_MEALS -> com.aircalc.converter.domain.model.FoodCategory.READY_MEALS
                    }

                    // Create conversion result with domain types
                    val result = com.aircalc.converter.domain.model.ConversionResult(
                        originalTemperature = ovenTemp,
                        originalTime = cookingTime,
                        airFryerTemperature = ovenTemp - 25,
                        airFryerTimeMinutes = (cookingTime * 0.8).toInt(),
                        temperatureUnit = domainTempUnit,
                        foodCategory = domainCategory,
                        cookingTip = domainCategory.cookingTip,
                        temperatureReduction = 25,
                        timeReduction = cookingTime - (cookingTime * 0.8).toInt()
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
                color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
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
                    color = PureWhite,
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
                    color = PureBlack
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
                    color = PureBlack
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
            containerColor = PureWhite
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
                color = PureBlack,
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
            containerColor = PureWhite
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
            containerColor = PrimaryGreen,
            contentColor = PureWhite,
            disabledContainerColor = MediumGray,
            disabledContentColor = PureWhite
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
                text = "Convert",
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
                text = "Convert to Air Fryer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun TemperatureInputSection(
    temperature: Int,
    onTemperatureChange: (Int) -> Unit,
    unit: TemperatureUnit
) {
    val minTemp = if (unit == TemperatureUnit.CELSIUS) 120 else 250
    val maxTemp = if (unit == TemperatureUnit.CELSIUS) 250 else 480
    val increment = 5
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(temperature.toString()) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MediumGray),
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
            FilledIconButton(
                onClick = {
                    if (temperature > minTemp) onTemperatureChange(temperature - increment)
                },
                modifier = Modifier
                    .size(70.dp)
                    .semantics { contentDescription = "Decrease temperature" },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = CircleShape
            ) {
                Text(
                    text = "−",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Center section: Icon + Title + Value (editable)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isEditing = true
                        textValue = temperature.toString()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_temperature),
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Temperature",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = PureBlack
                        )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    val textFieldValue = remember {
                        mutableStateOf(
                            androidx.compose.ui.text.input.TextFieldValue(
                                text = textValue,
                                selection = androidx.compose.ui.text.TextRange(0, textValue.length)
                            )
                        )
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = textFieldValue.value,
                        onValueChange = { newValue ->
                            // Only allow digits
                            if (newValue.text.all { it.isDigit() } || newValue.text.isEmpty()) {
                                val intValue = newValue.text.toIntOrNull()

                                // If value exceeds max, clamp it to max immediately
                                if (intValue != null && intValue > maxTemp) {
                                    val clampedText = maxTemp.toString()
                                    textFieldValue.value = androidx.compose.ui.text.input.TextFieldValue(
                                        text = clampedText,
                                        selection = androidx.compose.ui.text.TextRange(clampedText.length)
                                    )
                                    textValue = clampedText
                                } else {
                                    textFieldValue.value = newValue
                                    textValue = newValue.text
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureBlack,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                val intValue = textValue.toIntOrNull() ?: temperature
                                val clampedValue = intValue.coerceIn(minTemp, maxTemp)
                                onTemperatureChange(clampedValue)
                                isEditing = false
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .width(120.dp)
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Text(
                        text = "$temperature${unit.symbol}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack,
                        modifier = Modifier.semantics {
                            contentDescription = "Current temperature $temperature ${unit.symbol}"
                        }
                    )
                }
            }

            // Far right: Plus button
            FilledIconButton(
                onClick = {
                    if (temperature < maxTemp) onTemperatureChange(temperature + increment)
                },
                modifier = Modifier
                    .size(70.dp)
                    .semantics { contentDescription = "Increase temperature" },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = CircleShape
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
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
    val minTime = 1
    val maxTime = 180
    val increment = 1
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(time.toString()) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MediumGray),
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
            FilledIconButton(
                onClick = {
                    if (time > minTime) onTimeChange(maxOf(minTime, time - increment))
                },
                modifier = Modifier
                    .size(70.dp)
                    .semantics { contentDescription = "Decrease cooking time" },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = CircleShape
            ) {
                Text(
                    text = "−",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Center section: Icon + Title + Value (editable)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isEditing = true
                        textValue = time.toString()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cooking time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = PureBlack
                        )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    val textFieldValue = remember {
                        mutableStateOf(
                            androidx.compose.ui.text.input.TextFieldValue(
                                text = textValue,
                                selection = androidx.compose.ui.text.TextRange(0, textValue.length)
                            )
                        )
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = textFieldValue.value,
                        onValueChange = { newValue ->
                            // Only allow digits
                            if (newValue.text.all { it.isDigit() } || newValue.text.isEmpty()) {
                                val intValue = newValue.text.toIntOrNull()

                                // If value exceeds max, clamp it to max immediately
                                if (intValue != null && intValue > maxTime) {
                                    val clampedText = maxTime.toString()
                                    textFieldValue.value = androidx.compose.ui.text.input.TextFieldValue(
                                        text = clampedText,
                                        selection = androidx.compose.ui.text.TextRange(clampedText.length)
                                    )
                                    textValue = clampedText
                                } else {
                                    textFieldValue.value = newValue
                                    textValue = newValue.text
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureBlack,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                val intValue = textValue.toIntOrNull() ?: time
                                val clampedValue = intValue.coerceIn(minTime, maxTime)
                                onTimeChange(clampedValue)
                                isEditing = false
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .width(120.dp)
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Text(
                        text = "${time}m",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack,
                        modifier = Modifier.semantics {
                            contentDescription = "Current cooking time $time minutes"
                        }
                    )
                }
            }

            // Far right: Plus button
            FilledIconButton(
                onClick = {
                    if (time < maxTime) onTimeChange(minOf(maxTime, time + increment))
                },
                modifier = Modifier
                    .size(70.dp)
                    .semantics { contentDescription = "Increase cooking time" },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = CircleShape
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
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
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) androidx.compose.ui.graphics.Color(0xFFFFF8E6) else PureWhite
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryRed else MediumGray
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
                tint = PrimaryRed,
                modifier = Modifier
                    .size(36.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = getCategoryDisplayName(category),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = PureBlack,
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
        else -> "🍽️"
    }
}

private fun getCategoryIconResource(category: FoodCategory): Int {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> R.drawable.ic_ready_meals
        FoodCategory.MEATS_RAW -> R.drawable.ic_raw_meat
        FoodCategory.FRESH_VEGETABLES -> R.drawable.ic_veg
        FoodCategory.FROZEN_FOODS -> R.drawable.ic_frozen
        else -> R.drawable.ic_ready_meals
    }
}

private fun getCategoryDescription(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> "Pre-cooked meals that are stored in the refrigerator. These typically require reheating and may need reduced cooking times in an air fryer."
        FoodCategory.MEATS_RAW -> "Raw meat products including chicken, beef, pork, and fish. These require thorough cooking and proper temperature control for food safety."
        FoodCategory.FRESH_VEGETABLES -> "Fresh vegetables and produce. Most vegetables cook quickly in an air fryer and develop a nice crispy exterior while staying tender inside."
        FoodCategory.FROZEN_FOODS -> "Frozen items including vegetables, meats, and prepared foods. May require slightly longer cooking times but often cook more evenly from frozen."
        else -> "Select a food category to see cooking guidance and tips for air fryer conversion."
    }
}

private fun getCategoryDisplayName(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> "Ready meals"
        FoodCategory.MEATS_RAW -> "Raw meat"
        FoodCategory.FRESH_VEGETABLES -> "Veg"
        FoodCategory.FROZEN_FOODS -> "Frozen"
        else -> category.displayName
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
                    LightGray,
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
                    color = PureBlack
                )
                Text(
                    text = selectedCategory.description,
                    fontSize = 14.sp,
                    color = PureBlack.copy(alpha = 0.7f)
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