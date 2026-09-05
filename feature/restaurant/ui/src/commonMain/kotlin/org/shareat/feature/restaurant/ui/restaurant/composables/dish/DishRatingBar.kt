package org.shareat.feature.restaurant.ui.restaurant.composables.dish

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_rate_prompt
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_rate_star

private val RatingStars = 1..5
private val StarSize = 28.dp

@Composable
internal fun DishRatingBar(
    selectedRating: Int?,
    onRatingClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.restaurant_dish_rate_prompt),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        RatingStars.forEach { star ->
            RatingStar(
                star = star,
                isSelected = selectedRating != null && star <= selectedRating,
                onClick = { onRatingClick(star) },
            )
        }
    }
}

@Composable
private fun RatingStar(star: Int, isSelected: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
        contentDescription = stringResource(Res.string.restaurant_dish_rate_star, star),
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).size(StarSize),
    )
}

@Preview
@Composable
private fun DishRatingBarPreview() {
    ShareatTheme {
        DishRatingBar(selectedRating = 3, onRatingClick = {})
    }
}

@Preview
@Composable
private fun DishRatingBarUnratedPreview() {
    ShareatTheme {
        DishRatingBar(selectedRating = null, onRatingClick = {})
    }
}
