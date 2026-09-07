package org.shareat.feature.review

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.review.domain.SubmitDishReviewsParams
import org.shareat.feature.review.domain.SubmitDishReviewsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DishReviewViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateCannotBeSubmitted() {
        val viewModel = viewModelFor()

        assertFalse(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun formChangesAreReflectedInState() {
        val viewModel = viewModelFor()

        viewModel.onDishRatingChange(4)
        viewModel.onDishCommentChange("Muy bueno")
        viewModel.onRestaurantRatingChange(5)
        viewModel.onRestaurantCommentChange("Volveremos")

        assertEquals(
            DishReviewUiState(
                dishRating = 4,
                dishComment = "Muy bueno",
                restaurantRating = 5,
                restaurantComment = "Volveremos",
            ),
            viewModel.uiState.value,
        )
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun outOfRangeRatingsAreIgnored() {
        val viewModel = viewModelFor()

        viewModel.onDishRatingChange(0)
        viewModel.onRestaurantRatingChange(6)

        assertEquals(0, viewModel.uiState.value.dishRating)
        assertEquals(0, viewModel.uiState.value.restaurantRating)
    }

    @Test
    fun submitSendsTheFormToTheUseCaseAndMarksSuccess() = runTest(dispatcher) {
        var received: SubmitDishReviewsParams? = null
        val viewModel = viewModelFor { params ->
            received = params
            RepositoryResult.Success(Unit)
        }
        viewModel.onDishRatingChange(4)
        viewModel.onDishCommentChange("Crujiente")
        viewModel.onRestaurantRatingChange(5)
        viewModel.onRestaurantCommentChange("Volveremos")

        viewModel.onSubmitClick()
        assertTrue(viewModel.uiState.value.isSubmitting)
        advanceUntilIdle()

        assertEquals(
            SubmitDishReviewsParams(
                dishId = TestDishId,
                dishRating = 4,
                dishComment = "Crujiente",
                restaurantRating = 5,
                restaurantComment = "Volveremos",
            ),
            received,
        )
        assertTrue(viewModel.uiState.value.submitSucceeded)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submitFailureShowsAnErrorAndAllowsRetry() = runTest(dispatcher) {
        val viewModel = viewModelFor {
            RepositoryResult.Failure(RepositoryError.Offline)
        }
        viewModel.onDishRatingChange(4)
        viewModel.onRestaurantRatingChange(5)

        viewModel.onSubmitClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.submitSucceeded)
        assertTrue(viewModel.uiState.value.canSubmit)
        assertIs<String>(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun editingAfterSuccessClearsSuccessFeedback() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        viewModel.onDishRatingChange(4)
        viewModel.onRestaurantRatingChange(5)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.submitSucceeded)

        viewModel.onDishCommentChange("Actualizado")

        assertFalse(viewModel.uiState.value.submitSucceeded)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.canSubmit)
    }
}

private val TestDishId = DishId("dish-1")

private fun viewModelFor(
    submit: SubmitDishReviewsUseCase = SubmitDishReviewsUseCase {
        RepositoryResult.Success(Unit)
    },
): DishReviewViewModel = DishReviewViewModel(
    dishId = TestDishId,
    submitDishReviews = submit,
)
