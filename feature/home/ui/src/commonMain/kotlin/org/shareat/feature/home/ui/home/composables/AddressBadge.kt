package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun AddressBadge(
    address: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp).padding(end = 4.dp),
        )
        Text(
            text = address,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun AddressBadgePreview() {
    ShareatTheme {
        AddressBadge(address = "Calle del Olmo, 18, Madrid")
    }
}
