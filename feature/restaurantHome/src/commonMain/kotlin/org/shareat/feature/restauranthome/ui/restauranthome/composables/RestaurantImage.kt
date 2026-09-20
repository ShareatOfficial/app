package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomePreviewImagePrefix
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.baratie_bouillabaisse
import shareat.feature.restauranthome.ui.generated.resources.baratie_fish_and_chips
import shareat.feature.restauranthome.ui.generated.resources.baratie_grilled_octopus
import shareat.feature.restauranthome.ui.generated.resources.baratie_hero
import shareat.feature.restauranthome.ui.generated.resources.baratie_ocean_panna_cotta
import shareat.feature.restauranthome.ui.generated.resources.baratie_seafood_fried_rice
import shareat.feature.restauranthome.ui.generated.resources.baratie_tuna_steak
import shareat.feature.restauranthome.ui.generated.resources.restaurant_placeholder_hero

/**
 * Loads network images in production and packaged drawable resources in previews.
 *
 * Preview data intentionally stores stable pseudo-URLs instead of calling `Res.getUri` while a
 * global preview-data object is being initialized. Android Studio has no resource reader at that
 * point, which causes previews to fail before composition starts.
 */
@Composable
internal fun RestaurantImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    colorFilter: ColorFilter? = null,
) {
    val previewResource = imageUrl?.toPreviewDrawableResource()
    if (previewResource != null) {
        Image(
            painter = painterResource(previewResource),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
        )
    } else {
        val fallback = painterResource(Res.drawable.restaurant_placeholder_hero)
        AsyncImage(
            model = imageUrl?.ifBlank { null },
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = fallback,
            error = fallback,
            fallback = fallback,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
        )
    }
}

private fun String.toPreviewDrawableResource(): DrawableResource? {
    if (!startsWith(RestaurantHomePreviewImagePrefix)) return null
    return when (removePrefix(RestaurantHomePreviewImagePrefix)) {
        "baratie_hero.png" -> Res.drawable.baratie_hero
        "baratie_seafood_fried_rice.png" -> Res.drawable.baratie_seafood_fried_rice
        "baratie_tuna_steak.png" -> Res.drawable.baratie_tuna_steak
        "baratie_bouillabaisse.png" -> Res.drawable.baratie_bouillabaisse
        "baratie_fish_and_chips.png" -> Res.drawable.baratie_fish_and_chips
        "baratie_ocean_panna_cotta.png" -> Res.drawable.baratie_ocean_panna_cotta
        "baratie_grilled_octopus.png" -> Res.drawable.baratie_grilled_octopus
        else -> null
    }
}
