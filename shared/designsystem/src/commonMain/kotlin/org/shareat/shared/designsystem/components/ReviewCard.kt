package org.shareat.shared.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.theme.ShareatTheme

private val ReviewCardWidth = 220.dp
private const val CommentLines = 2

@Composable
fun ReviewCard(
    comment: String,
    rating: Int,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Card(
        modifier = modifier.width(ReviewCardWidth),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                RatingBadge(ratingLabel = rating.toString())
            }
            Text(
                text = comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = CommentLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun ReviewCardWithTitlePreview() {
    ShareatTheme {
        ReviewCard(
            title = "Pulpo a la brasa",
            comment = "Tiernísimo y con el punto justo de humo.",
            rating = 5,
        )
    }
}

@Preview
@Composable
private fun ReviewCardPreview() {
    ShareatTheme {
        ReviewCard(
            comment = "Tiernísimo y con el punto justo de humo.",
            rating = 5,
        )
    }
}
