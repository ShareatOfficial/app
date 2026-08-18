package org.shareat.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import shareat.feature.login.generated.resources.Res
import shareat.feature.login.generated.resources.high_quality_immersive_vertical_food_photography_for_a_restaurant_app_welcome

private val welcomeBackground: DrawableResource
    get() = Res.drawable.high_quality_immersive_vertical_food_photography_for_a_restaurant_app_welcome

/**
 * Full-bleed welcome photography with a translucent panel anchored to the bottom.
 *
 * The photo is bright and busy through the middle, so [content] sits on a [Surface] instead of
 * directly over the image: everything inside can keep using the plain `onSurface` roles and stay
 * legible in both light and dark themes.
 */
@Composable
internal fun LoginBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val systemBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(welcomeBackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        )
        // Gradiente on the systembars to let it always visible. It would be good to move it to all displays or something
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(systemBarHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Transparent
                        )
                    )
                )
        )

        // General dark overlay to make text more readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Blends the seam between the photography and the panel below it.
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1.0f to MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                ),
            ),
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
    }
}
