package org.shareat.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.shared.media.ImageUploadValidationResult
import org.shareat.shared.media.compressedAsJpeg
import org.shareat.shared.media.preparedImageUpload
import shareat.feature.review.generated.resources.Res
import shareat.feature.review.generated.resources.unlisted_review_comment
import shareat.feature.review.generated.resources.unlisted_review_dish_name
import shareat.feature.review.generated.resources.unlisted_review_error_forbidden
import shareat.feature.review.generated.resources.unlisted_review_error_image_invalid
import shareat.feature.review.generated.resources.unlisted_review_error_image_size
import shareat.feature.review.generated.resources.unlisted_review_error_image_type
import shareat.feature.review.generated.resources.unlisted_review_error_offline
import shareat.feature.review.generated.resources.unlisted_review_error_session
import shareat.feature.review.generated.resources.unlisted_review_error_submit
import shareat.feature.review.generated.resources.unlisted_review_error_unavailable
import shareat.feature.review.generated.resources.unlisted_review_image_add
import shareat.feature.review.generated.resources.unlisted_review_image_change
import shareat.feature.review.generated.resources.unlisted_review_image_processing
import shareat.feature.review.generated.resources.unlisted_review_image_required
import shareat.feature.review.generated.resources.unlisted_review_image_selected
import shareat.feature.review.generated.resources.unlisted_review_rating
import shareat.feature.review.generated.resources.unlisted_review_restaurant_name
import shareat.feature.review.generated.resources.unlisted_review_submit
import shareat.feature.review.generated.resources.unlisted_review_submitted
import shareat.feature.review.generated.resources.unlisted_review_submitting
import shareat.feature.review.generated.resources.unlisted_review_star_accessibility
import shareat.feature.review.generated.resources.unlisted_review_star_not_selected
import shareat.feature.review.generated.resources.unlisted_review_star_selected
import shareat.feature.review.generated.resources.unlisted_review_title
import shareat.feature.review.generated.resources.unlisted_review_validation

@Composable
public fun UnlistedDishReviewScreen(
    onDismissRequest: () -> Unit,
    onReviewSubmitted: () -> Unit,
    openingToken: Any,
    modifier: Modifier = Modifier,
    viewModel: UnlistedDishReviewViewModel = koinViewModel(),
) {
    val collectedState by viewModel.uiState.collectAsState()
    val state = if (collectedState.openingToken === openingToken) {
        collectedState
    } else {
        UnlistedDishReviewUiState(openingToken = openingToken)
    }
    val scope = rememberCoroutineScope()
    val picker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null) {
            viewModel.onImagePreparationStarted()
            scope.launch { viewModel.onImagePrepared(file.toImageUploadValidationResult()) }
        }
    }
    LaunchedEffect(viewModel, openingToken) {
        viewModel.beginNewReview(openingToken)
        viewModel.reviewSubmitted.collect { onReviewSubmitted() }
    }
    UnlistedDishReviewScreenContent(
        state = state,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onRestaurantNameChange = viewModel::onRestaurantNameChange,
        onDishNameChange = viewModel::onDishNameChange,
        onChooseImage = picker::launch,
        onRatingChange = viewModel::onRatingChange,
        onCommentChange = viewModel::onCommentChange,
        onSubmit = viewModel::onSubmitClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnlistedDishReviewScreenContent(
    state: UnlistedDishReviewUiState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onRestaurantNameChange: (String) -> Unit = {},
    onDishNameChange: (String) -> Unit = {},
    onChooseImage: () -> Unit = {},
    onRatingChange: (Int) -> Unit = {},
    onCommentChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = { if (!state.isSubmitting) onDismissRequest() },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { target -> target != androidx.compose.material3.SheetValue.Hidden || !state.isSubmitting },
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(stringResource(Res.string.unlisted_review_title), style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = state.restaurantName,
                onValueChange = onRestaurantNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                label = { Text(stringResource(Res.string.unlisted_review_restaurant_name)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            OutlinedTextField(
                value = state.dishName,
                onValueChange = onDishNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                label = { Text(stringResource(Res.string.unlisted_review_dish_name)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.image != null) {
                    SubcomposeAsyncImage(
                        model = state.image.bytes,
                        contentDescription = state.image.alternativeText ?: stringResource(Res.string.unlisted_review_image_selected),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))
                            .semantics { contentDescription = "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = onChooseImage, enabled = !state.isSubmitting && !state.isPreparingImage) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(if (state.image == null) Res.string.unlisted_review_image_add else Res.string.unlisted_review_image_change))
                    }
                    if (state.isPreparingImage) {
                        Text(stringResource(Res.string.unlisted_review_image_processing), style = MaterialTheme.typography.bodySmall)
                    } else if (state.image == null) {
                        Text(stringResource(Res.string.unlisted_review_image_required), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            state.imageError?.let { error ->
                Text(
                    text = stringResource(
                        when (error) {
                            UnlistedDishReviewImageError.UNSUPPORTED_FORMAT -> Res.string.unlisted_review_error_image_type
                            UnlistedDishReviewImageError.TOO_LARGE -> Res.string.unlisted_review_error_image_size
                            UnlistedDishReviewImageError.INVALID_FILE -> Res.string.unlisted_review_error_image_invalid
                        },
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.unlisted_review_rating), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    (1..5).forEach { rating ->
                        val ratingDescription = stringResource(Res.string.unlisted_review_star_accessibility, rating)
                        val isSelected = state.isRatingSelected(rating)
                        val selectionDescription = stringResource(
                            if (isSelected) Res.string.unlisted_review_star_selected
                            else Res.string.unlisted_review_star_not_selected,
                        )
                        IconButton(
                            onClick = { onRatingChange(rating) },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = ratingDescription
                                selected = isSelected
                                stateDescription = selectionDescription
                            },
                        ) {
                            Icon(
                                imageVector = if (rating <= state.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (rating <= state.rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = state.comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                label = { Text(stringResource(Res.string.unlisted_review_comment)) },
                minLines = 3,
                maxLines = 6,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )

            state.error?.let { error ->
                Text(stringResource(error.resource()), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (state.submitSucceeded) {
                Text(stringResource(Res.string.unlisted_review_submitted), color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = onSubmit, enabled = state.canSubmit, modifier = Modifier.fillMaxWidth()) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(Res.string.unlisted_review_submitting))
                } else {
                    Text(stringResource(Res.string.unlisted_review_submit))
                }
            }
        }
    }
}

internal fun UnlistedDishReviewUiState.isRatingSelected(option: Int): Boolean =
    rating in 1..5 && option == rating

private suspend fun PlatformFile.toImageUploadValidationResult(): ImageUploadValidationResult =
    preparedImageUpload(fileName = name) { compression -> compressedAsJpeg(compression) }

@Composable
private fun UnlistedDishReviewError.resource() = when (this) {
    UnlistedDishReviewError.INVALID_CREDENTIALS,
    UnlistedDishReviewError.UNAUTHENTICATED -> Res.string.unlisted_review_error_session
    UnlistedDishReviewError.OFFLINE -> Res.string.unlisted_review_error_offline
    UnlistedDishReviewError.FORBIDDEN -> Res.string.unlisted_review_error_forbidden
    UnlistedDishReviewError.TEMPORARILY_UNAVAILABLE -> Res.string.unlisted_review_error_unavailable
    UnlistedDishReviewError.VALIDATION -> Res.string.unlisted_review_validation
    UnlistedDishReviewError.UNKNOWN -> Res.string.unlisted_review_error_submit
}
