package org.shareat.shared.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTest {
    @Test
    fun lightColorSchemeMatchesMaterialThemeBuilderExport() {
        with(ShareatLightColorScheme) {
            assertEquals(Color(0xFF8F4C36), primary)
            assertEquals(Color(0xFFFFFFFF), onPrimary)
            assertEquals(Color(0xFFFFDBD0), primaryContainer)
            assertEquals(Color(0xFF723521), onPrimaryContainer)
            assertEquals(Color(0xFF006874), secondary)
            assertEquals(Color(0xFFFFFFFF), onSecondary)
            assertEquals(Color(0xFF9EEFFD), secondaryContainer)
            assertEquals(Color(0xFF004F58), onSecondaryContainer)
            assertEquals(Color(0xFF4A672D), tertiary)
            assertEquals(Color(0xFFFFFFFF), onTertiary)
            assertEquals(Color(0xFFCBEEA5), tertiaryContainer)
            assertEquals(Color(0xFF334E17), onTertiaryContainer)
            assertEquals(Color(0xFFBA1A1A), error)
            assertEquals(Color(0xFFFFFFFF), onError)
            assertEquals(Color(0xFFFFDAD6), errorContainer)
            assertEquals(Color(0xFF93000A), onErrorContainer)
            assertEquals(Color(0xFFFFF8F6), background)
            assertEquals(Color(0xFF231917), onBackground)
            assertEquals(Color(0xFFF5FAFB), surface)
            assertEquals(Color(0xFF171D1E), onSurface)
            assertEquals(Color(0xFFF5DED7), surfaceVariant)
            assertEquals(Color(0xFF53433F), onSurfaceVariant)
            assertEquals(Color(0xFF85736E), outline)
            assertEquals(Color(0xFFD8C2BC), outlineVariant)
            assertEquals(Color(0xFF000000), scrim)
            assertEquals(Color(0xFF2B3133), inverseSurface)
            assertEquals(Color(0xFFECF2F3), inverseOnSurface)
            assertEquals(Color(0xFFFFB59E), inversePrimary)
            assertEquals(Color(0xFFD5DBDC), surfaceDim)
            assertEquals(Color(0xFFF5FAFB), surfaceBright)
            assertEquals(Color(0xFFFFFFFF), surfaceContainerLowest)
            assertEquals(Color(0xFFEFF5F6), surfaceContainerLow)
            assertEquals(Color(0xFFE9EFF0), surfaceContainer)
            assertEquals(Color(0xFFE3E9EA), surfaceContainerHigh)
            assertEquals(Color(0xFFDEE3E5), surfaceContainerHighest)
        }
    }
}
