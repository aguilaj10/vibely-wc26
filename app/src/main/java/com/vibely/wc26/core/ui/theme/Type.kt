package com.vibely.wc26.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Phase 0 typography — system defaults with sporty weight tuning.
// Custom display face (e.g. Bebas Neue / Anton) lands in Phase 8 polish.
private val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.SemiBold),
    headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
    ),
)
