package com.aircalc.converter.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aircalc.converter.R
import com.aircalc.converter.ui.theme.*
import com.aircalc.converter.domain.model.ConversionResult
import com.aircalc.converter.domain.model.FoodCategory
import com.aircalc.converter.domain.model.TemperatureUnit
import com.aircalc.converter.TimerState

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                text = "AirCalc",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PureBlack,
                textAlign = TextAlign.Center
            )

            // Spacer to balance the layout
            Spacer(modifier = Modifier.width(24.dp))
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Air fryer settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PureBlack,
                modifier = Modifier.semantics {
                    contentDescription = "Air fryer conversion results"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature
                Text(
                    text = "${conversionResult.airFryerTemperature}°",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack,
                    modifier = Modifier.semantics {
                        contentDescription = "Air fryer temperature ${conversionResult.airFryerTemperature} degrees"
                    }
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(androidx.compose.ui.graphics.Color(0xFFD1D5DB))
                )

                // Time
                Text(
                    text = "${conversionResult.airFryerTimeMinutes}m",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack,
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
                    color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
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

                // Background circle (light gray) - now using F0F0F0 as solid background
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                    radius = radius,
                    center = center
                )

                // Progress arc (red)
                if (progress > 0) {
                    drawArc(
                        color = androidx.compose.ui.graphics.Color(0xFFA31B1B),
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
                    color = PureBlack,
                    fontSize = 48.sp,
                    modifier = Modifier.semantics {
                        contentDescription = "Timer: ${formatTime(timerState.timeLeftSeconds)}"
                    }
                )

                Text(
                    text = "Remaining",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PureBlack
                )
            }
        }
    }
}

@Composable
private fun TimerControlButtons(
    timerState: TimerState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start/Pause/Resume Button
        var isPendingAction by remember { mutableStateOf(false) }

        if (!timerState.isRunning && timerState.timeLeftSeconds > 0) {
            // Start Button - Green
            Button(
                onClick = {
                    isPendingAction = true
                },
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
                    text = "Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isPendingAction) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(150)
                    onStartTimer()
                    isPendingAction = false
                }
            }
        } else if (timerState.isRunning) {
            // Stop Button - Green
            Button(
                onClick = {
                    isPendingAction = true
                },
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
                    text = "Stop",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isPendingAction) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(150)
                    onPauseTimer()
                    isPendingAction = false
                }
            }
        } else {
            // Resume Button - Green
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
                    text = "Resume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Reset Button - Cream background with red outline
        OutlinedButton(
            onClick = onResetTimer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CreamBackground,
                contentColor = PrimaryRed
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Reset",
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
                text = "Timer",
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = LightGray
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
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cooking tips:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get category-specific cooking tips
            val tips = when (category.id) {
                "frozen_foods" -> listOf(
                    "Shake basket halfway through cooking",
                    "No need to preheat for frozen foods",
                    "Don't overcrowd the basket for crispy results"
                )
                "fresh_vegetables" -> listOf(
                    "Toss vegetables halfway through for even cooking",
                    "Light coating of oil helps achieve crispiness",
                    "Cut vegetables to similar sizes for even cooking"
                )
                "raw_meats" -> listOf(
                    "Check internal temperature before serving",
                    "Let meat rest for 5 minutes after cooking",
                    "Pat meat dry before seasoning for best results"
                )
                "baked_goods" -> listOf(
                    "Use parchment paper and check frequently",
                    "Reduce temperature by 25°F from traditional recipes",
                    "Check doneness 2-3 minutes earlier than recipe states"
                )
                "ready_meals" -> listOf(
                    "Pierce any sealed packaging before cooking",
                    "Stir or rotate halfway through for even heating",
                    "Let stand for 1-2 minutes before serving"
                )
                else -> listOf(
                    "Preheat air fryer for 3-5 minutes for best results",
                    "Don't overcrowd the basket",
                    "Shake or flip food halfway through cooking"
                )
            }

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureBlack,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureBlack
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