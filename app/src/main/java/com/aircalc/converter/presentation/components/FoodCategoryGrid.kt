package com.aircalc.converter.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aircalc.converter.FoodCategory
import com.aircalc.converter.R
import com.aircalc.converter.ui.theme.*

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
private fun GridFoodCategoryCard(
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
        border = BorderStroke(
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

// Helper functions
private fun getCategoryIconResource(category: FoodCategory): Int {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> R.drawable.ic_ready_meals
        FoodCategory.MEATS_RAW -> R.drawable.ic_raw_meat
        FoodCategory.FRESH_VEGETABLES -> R.drawable.ic_veg
        FoodCategory.FROZEN_FOODS -> R.drawable.ic_frozen
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
fun getCategoryDescription(category: FoodCategory): String {
    return when (category) {
        FoodCategory.REFRIGERATED_READY_MEALS -> stringResource(R.string.refrigerated_ready_meals_desc)
        FoodCategory.MEATS_RAW -> stringResource(R.string.meats_raw_desc)
        FoodCategory.FRESH_VEGETABLES -> stringResource(R.string.fresh_vegetables_desc)
        FoodCategory.FROZEN_FOODS -> stringResource(R.string.frozen_foods_desc)
    }
}
