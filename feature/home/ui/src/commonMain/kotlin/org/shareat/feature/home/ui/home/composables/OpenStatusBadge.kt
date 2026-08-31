package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.theme.ShareatTheme

private val OpenStatusBadgeShape = RoundedCornerShape(12.dp)

@Composable
internal fun OpenStatusBadge(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = if (isOpen) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onError
    },
) {
    val containerColor = if (isOpen) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    Row(
        modifier = modifier
            .clip(OpenStatusBadgeShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(contentColor),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isOpen) "Open" else "Closed",
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview
@Composable
private fun OpenStatusBadgeOpenPreview() {
    ShareatTheme {
        OpenStatusBadge(isOpen = true)
    }
}

@Preview
@Composable
private fun OpenStatusBadgeClosedPreview() {
    ShareatTheme {
        OpenStatusBadge(isOpen = false)
    }
}
