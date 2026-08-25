package org.shareat.app.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTypography(): Typography {
    val default = Typography()
    return default.copy(
        displayLarge = default.displayLarge.copy(
            fontFamily = FrauncesFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        displayMedium = default.displayMedium.copy(
            fontFamily = FrauncesFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        displaySmall = default.displaySmall.copy(
            fontFamily = FrauncesFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        headlineLarge = default.headlineLarge.copy(
            fontFamily = FrauncesFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        headlineMedium = default.headlineMedium.copy(
            fontFamily = FrauncesFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        headlineSmall = default.headlineSmall.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        titleLarge = default.titleLarge.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        titleMedium = default.titleMedium.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Medium
        ),
        titleSmall = default.titleSmall.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = default.bodyLarge.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Normal
        ),
        bodyMedium = default.bodyMedium.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Normal
        ),
        bodySmall = default.bodySmall.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Normal
        ),
        labelLarge = default.labelLarge.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.SemiBold
        ),
        labelMedium = default.labelMedium.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Medium
        ),
        labelSmall = default.labelSmall.copy(
            fontFamily = InterFontFamily(),
            fontWeight = FontWeight.Medium
        )
    )
}
