package org.shareat.feature.review

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
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
                        errorMessage = null,
                    )
                    is RepositoryResult.Failure -> it.copy(
                        isSubmitting = false,
                        submitSucceeded = false,
                        errorMessage = result.error.toReviewMessage(),
                    )
                }
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
            else state.transform().copy(submitSucceeded = false, errorMessage = null)
        }
    }
}

private fun RepositoryError.toReviewMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "Las credenciales de tu sesión ya no son válidas."
    RepositoryError.Offline -> "No hay conexión. Comprueba tu red e inténtalo de nuevo."
    RepositoryError.Unauthenticated -> "Tu sesión ha caducado. Inicia sesión de nuevo."
    RepositoryError.Forbidden -> "Solo los clientes activos pueden publicar valoraciones."
    is RepositoryError.Unavailable -> "El servicio no está disponible temporalmente."
    is RepositoryError.AlreadyExists -> "La valoración ya existe."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "No se ha encontrado el plato que quieres valorar."
    is RepositoryError.Validation -> reason
}
