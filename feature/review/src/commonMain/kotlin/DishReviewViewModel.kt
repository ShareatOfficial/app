package org.shareat.feature.review

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.review.domain.SubmitDishReviewsParams
import org.shareat.feature.review.domain.SubmitDishReviewsUseCase

@Stable
@KoinViewModel
public class DishReviewViewModel(
    private val dishId: DishId,
    private val submitDishReviews: SubmitDishReviewsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DishReviewUiState())
    public val uiState: StateFlow<DishReviewUiState> = _uiState.asStateFlow()
    private val _reviewSubmitted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    public val reviewSubmitted: SharedFlow<Unit> = _reviewSubmitted.asSharedFlow()

    public fun startReview(initialDishRating: Int = 0) {
        if (_uiState.value.isSubmitting) return
        _uiState.value = DishReviewUiState(
            dishRating = initialDishRating.takeIf { it in DishReviewRatingRange } ?: 0,
        )
    }

    public fun onDishRatingChange(rating: Int) {
        updateRating(rating) { copy(dishRating = rating) }
    }

    public fun onDishCommentChange(comment: String) {
        edit { copy(dishComment = comment) }
    }

    public fun onRestaurantRatingChange(rating: Int) {
        updateRating(rating) { copy(restaurantRating = rating) }
    }

    public fun onRestaurantCommentChange(comment: String) {
        edit { copy(restaurantComment = comment) }
    }

    public fun onSubmitClick() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = submitDishReviews(
                SubmitDishReviewsParams(
                    dishId = dishId,
                    dishRating = state.dishRating,
                    dishComment = state.dishComment,
                    restaurantRating = state.restaurantRating,
                    restaurantComment = state.restaurantComment,
                ),
            )
            _uiState.update {
                when (result) {
                    is RepositoryResult.Success -> it.copy(
                        isSubmitting = false,
                        submitSucceeded = true,
                        error = null,
                    )
                    is RepositoryResult.Failure -> it.copy(
                        isSubmitting = false,
                        submitSucceeded = false,
                        error = result.error.toReviewError(),
                    )
                }
            }
            if (result is RepositoryResult.Success) {
                _reviewSubmitted.emit(Unit)
            }
        }
    }

    private inline fun updateRating(
        rating: Int,
        transform: DishReviewUiState.() -> DishReviewUiState,
    ) {
        if (rating !in DishReviewRatingRange) return
        edit(transform)
    }

    private inline fun edit(transform: DishReviewUiState.() -> DishReviewUiState) {
        _uiState.update { state ->
            if (state.isSubmitting) state
            else state.transform().copy(submitSucceeded = false, error = null)
        }
    }
}

private fun RepositoryError.toReviewError(): DishReviewError = when (this) {
    RepositoryError.InvalidCredentials -> DishReviewError.INVALID_CREDENTIALS
    RepositoryError.Offline -> DishReviewError.OFFLINE
    RepositoryError.Unauthenticated -> DishReviewError.UNAUTHENTICATED
    RepositoryError.Forbidden -> DishReviewError.FORBIDDEN
    is RepositoryError.Unavailable -> DishReviewError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.AlreadyExists -> DishReviewError.ALREADY_EXISTS
    is RepositoryError.NotFound -> DishReviewError.NOT_FOUND
    is RepositoryError.Conflict, is RepositoryError.Validation -> DishReviewError.UNKNOWN
}
