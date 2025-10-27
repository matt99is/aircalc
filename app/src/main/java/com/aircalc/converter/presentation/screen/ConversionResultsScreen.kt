package com.aircalc.converter.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aircalc.converter.R
import com.aircalc.converter.ui.theme.*
import com.aircalc.converter.domain.model.ConversionResult
import com.aircalc.converter.domain.model.FoodCategory
import com.aircalc.converter.domain.model.TemperatureUnit
import com.aircalc.converter.TimerState
import com.aircalc.converter.presentation.components.BorderCard

/**
 * Screen displaying conversion results with timer controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionResultsScreen(
    conversionResult: ConversionResult,
    timerState: TimerState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false // Feature flag for header
) {
    // Handle system back button press
    BackHandler(onBack = onNavigateBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header - hidden by default, can be enabled with feature flag
        if (showHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(24.dp)
                        .semantics {
                            contentDescription = "Go back to conversion input"
                        }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = PureBlack
                    )
                }

                // App title
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack,
                    textAlign = TextAlign.Center
                )

                // Spacer to balance the layout
                Spacer(modifier = Modifier.width(24.dp))
            }
        } else {
            // Add spacing when header is hidden
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Air fryer settings card
            AirFryerSettingsCard(
                conversionResult = conversionResult,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large circular timer - flexible size
            CircularTimerSection(
                timerState = timerState,
                targetMinutes = conversionResult.airFryerTimeMinutes,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tips Section
            CookingTipsCard(
                category = conversionResult.foodCategory,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            TimerControlButtons(
                timerState = timerState,
                targetMinutes = conversionResult.airFryerTimeMinutes,
                onStartTimer = onStartTimer,
                onPauseTimer = onPauseTimer,
                onResumeTimer = onResumeTimer,
                onResetTimer = onResetTimer,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AirFryerSettingsCard(
    conversionResult: ConversionResult,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else LightGray,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.air_fryer_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                modifier = Modifier
                    .testTag("resultsTitle")
                    .semantics {
                        contentDescription = "Air fryer conversion results"
                    }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature
                Text(
                    text = "${conversionResult.airFryerTemperature}°",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                    modifier = Modifier.semantics {
                        contentDescription = "Air fryer temperature ${conversionResult.airFryerTemperature} degrees"
                    }
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(
                            if (isDark) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
                            else PureBlack.copy(alpha = 0.3f)
                        )
                )

                // Time
                Text(
                    text = "${conversionResult.airFryerTimeMinutes}m",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                    modifier = Modifier.semantics {
                        contentDescription = "Air fryer cooking time ${conversionResult.airFryerTimeMinutes} minutes"
                    }
                )
            }
        }
    }
}

@Composable
private fun CircularTimerSection(
    timerState: TimerState,
    targetMinutes: Int,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val circleSize = minOf(maxWidth, maxHeight)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(circleSize)
                .background(
                    color = if (isDark) DarkInputBackground else LightGray,
                    shape = CircleShape
                )
        ) {
            // Circular progress ring
            val totalSeconds = targetMinutes * 60
            val progress = if (totalSeconds > 0) {
                1f - (timerState.timeLeftSeconds.toFloat() / totalSeconds.toFloat())
            } else 0f

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background circle (black for dark mode)
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    radius = radius,
                    center = center
                )

                // Progress arc (red)
                if (progress > 0) {
                    drawArc(
                        color = PrimaryRed,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                }
            }

            // Timer text in center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(timerState.timeLeftSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 48.sp,
                    modifier = Modifier.semantics {
                        contentDescription = "Timer: ${formatTime(timerState.timeLeftSeconds)}"
                    }
                )

                Text(
                    text = stringResource(R.string.remaining),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun TimerControlButtons(
    timerState: TimerState,
    targetMinutes: Int,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Determine button state based on timer
        val targetSeconds = targetMinutes * 60
        val isAtInitialTime = timerState.timeLeftSeconds == targetSeconds

        // Start/Pause/Resume Button
        if (!timerState.isRunning && isAtInitialTime) {
            // Start Button - Green (initial state)
            Button(
                onClick = onStartTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (timerState.isRunning) {
            // Pause Button - Green (timer running)
            Button(
                onClick = onPauseTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.pause),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Resume Button - Green (timer paused)
            Button(
                onClick = onResumeTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.resume_btn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Reset Button
        OutlinedButton(
            onClick = onResetTimer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else CreamBackground,
                contentColor = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PrimaryRed
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PrimaryRed
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.reset_btn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimerSection(
    timerState: TimerState,
    targetMinutes: Int,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    BorderCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.timer),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Display
            Text(
                text = formatTime(timerState.timeLeftSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    contentDescription = "Timer: ${formatTime(timerState.timeLeftSeconds)}"
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start/Resume/Pause Button
                if (!timerState.isRunning && timerState.timeLeftSeconds > 0) {
                    // Start Timer
                    Button(
                        onClick = onStartTimer,
                        modifier = Modifier.semantics {
                            contentDescription = "Start timer for $targetMinutes minutes"
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Timer")
                    }
                } else if (timerState.isRunning) {
                    // Pause Timer
                    Button(
                        onClick = onPauseTimer,
                        modifier = Modifier.semantics {
                            contentDescription = "Pause timer"
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause")
                    }
                } else {
                    // Resume Timer
                    Button(
                        onClick = onResumeTimer,
                        modifier = Modifier.semantics {
                            contentDescription = "Resume timer"
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resume")
                    }
                }

                // Reset Button
                OutlinedButton(
                    onClick = onResetTimer,
                    modifier = Modifier.semantics {
                        contentDescription = "Reset timer"
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset")
                }
            }

            // Progress indicator
            if (targetMinutes > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                val totalSeconds = targetMinutes * 60
                LinearProgressIndicator(
                    progress = { 1f - (timerState.timeLeftSeconds.toFloat() / totalSeconds.toFloat()) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CookingTipsCard(
    category: FoodCategory,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else LightGray
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lightbulb icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_lightbulb),
                    contentDescription = null,
                    tint = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.cooking_tips),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Get category-specific cooking tips
            val tips = when (category.id) {
                "frozen_foods" -> listOf(
                    "Shake basket halfway through",
                    "No need to preheat",
                    "Don't overcrowd for crispy results"
                )
                "fresh_vegetables" -> listOf(
                    "Toss halfway through for even cooking",
                    "Light oil coating helps crispiness",
                    "Cut to similar sizes"
                )
                "raw_meats" -> listOf(
                    "Check internal temperature",
                    "Let rest 5 minutes after cooking",
                    "Pat dry before seasoning"
                )
                "ready_meals" -> listOf(
                    "Pierce sealed packaging first",
                    "Stir or rotate halfway through",
                    "Let stand 1-2 minutes before serving"
                )
                else -> listOf(
                    "Preheat 3-5 minutes",
                    "Don't overcrowd basket",
                    "Shake halfway through"
                )
            }

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else PureBlack
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}