package org.shareat.feature.review

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.review.domain.SubmitUnlistedDishReviewUseCase
import org.shareat.feature.review.domain.model.SubmitUnlistedDishReviewParams
import org.shareat.shared.media.ImageUploadValidationResult

@Stable
@KoinViewModel
public class UnlistedDishReviewViewModel(
    private val submitReview: SubmitUnlistedDishReviewUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UnlistedDishReviewUiState())
    public val uiState: StateFlow<UnlistedDishReviewUiState> = _uiState.asStateFlow()
    private val _reviewSubmitted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    public val reviewSubmitted: SharedFlow<Unit> = _reviewSubmitted.asSharedFlow()
    private var activeOpeningToken: Any? = null

    /** Starts a clean form once for each presentation, even when Koin retains this ViewModel. */
    public fun beginNewReview(openingToken: Any) {
        if (activeOpeningToken === openingToken || _uiState.value.isSubmitting) return
        activeOpeningToken = openingToken
        _uiState.value = UnlistedDishReviewUiState(openingToken = openingToken)
    }

    public fun onRestaurantNameChange(value: String) = edit {
        copy(restaurantName = value.take(UnlistedDishReviewUiState.MAX_RESTAURANT_NAME_LENGTH))
    }
    public fun onDishNameChange(value: String) = edit {
        copy(dishName = value.take(UnlistedDishReviewUiState.MAX_DISH_NAME_LENGTH))
    }
    public fun onRatingChange(value: Int) {
        if (value in 1..5) edit { copy(rating = value) }
    }
    public fun onCommentChange(value: String) = edit {
        copy(comment = value.take(UnlistedDishReviewUiState.MAX_COMMENT_LENGTH))
    }

    public fun onImagePreparationStarted() {
        _uiState.update { if (it.isSubmitting) it else it.copy(isPreparingImage = true, imageError = null, error = null) }
    }

    public fun onImagePrepared(result: ImageUploadValidationResult) {
        _uiState.update { state ->
            if (state.isSubmitting) state else when (result) {
                is ImageUploadValidationResult.Success -> state.copy(
                    image = result.upload,
                    isPreparingImage = false,
                    imageError = null,
                    error = null,
                )
                ImageUploadValidationResult.UnsupportedFormat -> state.copy(isPreparingImage = false, imageError = UnlistedDishReviewImageError.UNSUPPORTED_FORMAT)
                ImageUploadValidationResult.TooLarge -> state.copy(isPreparingImage = false, imageError = UnlistedDishReviewImageError.TOO_LARGE)
                ImageUploadValidationResult.InvalidFile -> state.copy(isPreparingImage = false, imageError = UnlistedDishReviewImageError.INVALID_FILE)
            }
        }
    }

    public fun onSubmitClick() {
        val state = _uiState.value
        val image = state.image ?: return
        if (!state.canSubmit) return
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = submitReview(
                SubmitUnlistedDishReviewParams(
                    restaurantName = state.restaurantName,
                    dishName = state.dishName,
                    image = image,
                    rating = state.rating,
                    comment = state.comment,
                ),
            )
            _uiState.update {
                when (result) {
                    is RepositoryResult.Success -> it.copy(isSubmitting = false, submitSucceeded = true, error = null)
                    is RepositoryResult.Failure -> it.copy(isSubmitting = false, submitSucceeded = false, error = result.error.toUnlistedDishReviewError())
                }
            }
            if (result is RepositoryResult.Success) _reviewSubmitted.emit(Unit)
        }
    }

    private inline fun edit(transform: UnlistedDishReviewUiState.() -> UnlistedDishReviewUiState) {
        _uiState.update { state ->
            if (state.isSubmitting) state else state.transform().copy(submitSucceeded = false, error = null)
        }
    }
}

private fun RepositoryError.toUnlistedDishReviewError(): UnlistedDishReviewError = when (this) {
    RepositoryError.InvalidCredentials -> UnlistedDishReviewError.INVALID_CREDENTIALS
    RepositoryError.Offline -> UnlistedDishReviewError.OFFLINE
    RepositoryError.Unauthenticated -> UnlistedDishReviewError.UNAUTHENTICATED
    RepositoryError.Forbidden -> UnlistedDishReviewError.FORBIDDEN
    is RepositoryError.Unavailable -> UnlistedDishReviewError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.Validation -> UnlistedDishReviewError.VALIDATION
    is RepositoryError.AlreadyExists, is RepositoryError.Conflict, is RepositoryError.NotFound -> UnlistedDishReviewError.UNKNOWN
}
