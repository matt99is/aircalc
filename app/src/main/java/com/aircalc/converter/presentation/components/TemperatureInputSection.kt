package com.aircalc.converter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aircalc.converter.R
import com.aircalc.converter.TemperatureUnit
import com.aircalc.converter.presentation.modifiers.holdToRepeat
import com.aircalc.converter.ui.theme.MediumGray
import com.aircalc.converter.ui.theme.PrimaryRed
import com.aircalc.converter.ui.theme.PureBlack
import com.aircalc.converter.ui.theme.PureWhite

@Composable
fun TemperatureInputSection(
    temperature: Int,
    onTemperatureChange: (Int) -> Unit,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val minTemp = if (unit == TemperatureUnit.CELSIUS) 120 else 250
    val maxTemp = if (unit == TemperatureUnit.CELSIUS) 250 else 480
    val increment = 5

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
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
