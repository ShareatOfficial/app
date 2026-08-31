package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun RatingBadge(
    ratingLabel: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp).padding(end = 2.dp),
        )
        Text(
            text = ratingLabel,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview
@Composable
private fun RatingBadgePreview() {
    ShareatTheme {
        RatingBadge(ratingLabel = "4.8")
    }
}
