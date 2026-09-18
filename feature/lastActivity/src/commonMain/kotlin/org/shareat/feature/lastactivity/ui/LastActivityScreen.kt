package org.shareat.feature.lastactivity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.shared.designsystem.layout.safeDrawingTopPadding
import org.shareat.shared.designsystem.shimmerEffect

@Composable
fun LastActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: LastActivityViewModel = koinViewModel(),
    navigation: LastActivityNavigation = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) { viewModel.onScreenVisible() }
    LastActivityScreenStateless(state, navigation::openLogin, viewModel::retry, modifier)
}

@Composable
internal fun LastActivityScreenStateless(
    state: LastActivityUiState,
    onLoginClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingTopPadding()) {
            Text(
                text = "Actividad",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    LastActivityUiState.Initializing, LastActivityUiState.Loading -> ActivityLoading()
                    LastActivityUiState.Guest -> GuestActivity(onLoginClick, Modifier.align(Alignment.Center))
                    LastActivityUiState.Empty -> ActivityEmpty(Modifier.align(Alignment.Center))
                    is LastActivityUiState.Error -> ActivityError(
                        state.message, onRetryClick, Modifier.align(Alignment.Center),
                    )
                    is LastActivityUiState.Content -> ActivityList(state.items)
                }
            }
        }
    }
}

@Composable
private fun ActivityList(items: List<LastActivityReviewUiState>) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id.value }) { item -> ActivityReviewCard(item) }
    }
}

@Composable
private fun ActivityReviewCard(item: LastActivityReviewUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            if (item.imageUrl == null) {
                ActivityImagePlaceholder(item.name)
            } else {
                SubcomposeAsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.imageDescription ?: item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)),
                    loading = { ActivityImagePlaceholder(item.name) },
                    error = { ActivityImagePlaceholder(item.name) },
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                item.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                RatingStars(item.rating)
                item.comment?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityImagePlaceholder(name: String) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = "Imagen no disponible de $name" },
    )
}

@Composable
private fun RatingStars(rating: Int) {
    Row(
        modifier = Modifier.padding(top = 6.dp).semantics {
            contentDescription = "$rating de 5 estrellas"
        },
    ) {
        repeat(5) { index ->
            val filled = index < rating
            androidx.compose.material3.Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun GuestActivity(onLoginClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onLoginClick, modifier = modifier.padding(24.dp)) {
        Text("Si quieres ver tus últimas reviews, loguéate.")
    }
}

@Composable
private fun ActivityEmpty(modifier: Modifier = Modifier) {
    Text("Aún no has escrito ninguna review.", modifier = modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun ActivityError(message: String, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetryClick) { Text("Reintentar") }
    }
}

@Composable
private fun ActivityLoading() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) { ActivityReviewCardSkeleton() }
    }
}

@Composable
private fun ActivityReviewCardSkeleton() {
    val shimmer = MaterialTheme.colorScheme.surfaceVariant
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect(shimmer))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth(0.25f).height(12.dp).shimmerEffect(shimmer, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth(0.7f).height(20.dp).shimmerEffect(shimmer, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth().height(12.dp).shimmerEffect(shimmer, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth(0.45f).height(18.dp).shimmerEffect(shimmer, RoundedCornerShape(4.dp)))
            }
        }
    }
}
