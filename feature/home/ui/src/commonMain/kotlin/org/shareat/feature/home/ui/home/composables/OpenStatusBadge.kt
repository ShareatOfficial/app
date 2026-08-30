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

private val OpenStatusBackgroundColor = Color(0xFF2E7D32)
private val ClosedStatusBackgroundColor = Color(0xFFC62828)
private val OpenStatusBadgeShape = RoundedCornerShape(12.dp)

@Composable
internal fun OpenStatusBadge(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    Row(
        modifier = modifier
            .clip(OpenStatusBadgeShape)
            .background(if (isOpen) OpenStatusBackgroundColor else ClosedStatusBackgroundColor)
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
    MaterialTheme {
        OpenStatusBadge(isOpen = true)
    }
}

@Preview
@Composable
private fun OpenStatusBadgeClosedPreview() {
    MaterialTheme {
        OpenStatusBadge(isOpen = false)
    }
}
