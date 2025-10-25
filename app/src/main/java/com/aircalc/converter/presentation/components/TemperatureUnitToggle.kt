package com.aircalc.converter.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aircalc.converter.TemperatureUnit
import com.aircalc.converter.ui.theme.LightGray

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
