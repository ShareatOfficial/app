package org.shareat.shared.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import shareat.shared.designsystem.generated.resources.Fraunces_72pt_Bold
import shareat.shared.designsystem.generated.resources.Fraunces_72pt_Regular
import shareat.shared.designsystem.generated.resources.Fraunces_72pt_SemiBold
import shareat.shared.designsystem.generated.resources.Inter_18pt_Bold
import shareat.shared.designsystem.generated.resources.Inter_18pt_Medium
import shareat.shared.designsystem.generated.resources.Inter_18pt_Regular
import shareat.shared.designsystem.generated.resources.Inter_18pt_SemiBold
import shareat.shared.designsystem.generated.resources.Res

@Composable
internal fun InterFontFamily(): FontFamily = FontFamily(
    Font(resource = Res.font.Inter_18pt_Regular, weight = FontWeight.Normal),
    Font(resource = Res.font.Inter_18pt_Medium, weight = FontWeight.Medium),
    Font(resource = Res.font.Inter_18pt_SemiBold, weight = FontWeight.SemiBold),
    Font(resource = Res.font.Inter_18pt_Bold, weight = FontWeight.Bold),
)

@Composable
internal fun FrauncesFontFamily(): FontFamily = FontFamily(
    Font(resource = Res.font.Fraunces_72pt_Regular, weight = FontWeight.Normal),
    Font(resource = Res.font.Fraunces_72pt_SemiBold, weight = FontWeight.SemiBold),
    Font(resource = Res.font.Fraunces_72pt_Bold, weight = FontWeight.Bold),
)
