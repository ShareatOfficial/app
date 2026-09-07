package org.shareat.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.shareat.app.domain.model.DishId
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
fun DishReviewScreen(
    dishId: DishId,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DishReviewViewModel = koinViewModel(
        parameters = { parametersOf(dishId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    DishReviewScreenContent(
        uiState = uiState,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onDishRatingChange = viewModel::onDishRatingChange,
        onDishCommentChange = viewModel::onDishCommentChange,
        onRestaurantRatingChange = viewModel::onRestaurantRatingChange,
        onRestaurantCommentChange = viewModel::onRestaurantCommentChange,
        onSubmit = viewModel::onSubmitClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DishReviewScreenContent(
    uiState: DishReviewUiState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onDishRatingChange: (Int) -> Unit = {},
    onDishCommentChange: (String) -> Unit = {},
    onRestaurantRatingChange: (Int) -> Unit = {},
    onRestaurantCommentChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Cuéntanos qué te ha parecido",
                style = MaterialTheme.typography.headlineSmall,
            )

            ReviewSection(
                title = "Valora el plato",
                rating = uiState.dishRating,
                comment = uiState.dishComment,
                enabled = !uiState.isSubmitting,
                onRatingChange = onDishRatingChange,
                onCommentChange = onDishCommentChange,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReviewSection(
                title = "Valora el restaurante",
                rating = uiState.restaurantRating,
                comment = uiState.restaurantComment,
                enabled = !uiState.isSubmitting,
                onRatingChange = onRestaurantRatingChange,
                onCommentChange = onRestaurantCommentChange,
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSubmit,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = "Enviar valoraciones")
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (uiState.submitSucceeded) {
                Text(
                    text = "Tus valoraciones se han guardado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    rating: Int,
    comment: String,
    enabled: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        StarRating(
            rating = rating,
            enabled = enabled,
            onRatingChange = onRatingChange,
        )

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text(text = "Comentario (opcional)") },
            minLines = 3,
            maxLines = 5,
        )
    }
}

@Composable
private fun StarRating(
    rating: Int,
    enabled: Boolean,
    onRatingChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DishReviewRatingRange.forEach { star ->
            IconButton(
                onClick = { onRatingChange(star) },
                enabled = enabled,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (star == DishReviewRatingRange.first) {
                        "$star estrella"
                    } else {
                        "$star estrellas"
                    },
                    tint = if (star <= rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DishReviewScreenPreview() {
    ShareatTheme {
        DishReviewScreenContent(
            uiState = DishReviewUiState(
                dishRating = 4,
                dishComment = "Muy sabroso y bien presentado.",
                restaurantRating = 5,
            ),
        )
    }
}

@Preview(
    name = "Animación del modal",
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun DishReviewScreenAnimationPreview() {
    ShareatTheme {
        var showReview by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Button(onClick = { showReview = true }) {
                Text(text = "Abrir valoración")
            }

            if (showReview) {
                DishReviewScreenContent(
                    uiState = DishReviewUiState(),
                    onDismissRequest = { showReview = false },
                )
            }
        }
    }
}
