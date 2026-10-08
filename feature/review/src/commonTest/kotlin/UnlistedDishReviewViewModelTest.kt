package org.shareat.feature.review

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.review.domain.SubmitUnlistedDishReviewUseCase
import org.shareat.feature.review.domain.model.SubmitUnlistedDishReviewParams
import org.shareat.shared.media.ImageUploadValidationResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnlistedDishReviewViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun requiredFieldsAndImagePreparationGateSubmission() {
        val viewModel = viewModelFor()
        viewModel.onRestaurantNameChange("Restaurante")
        viewModel.onDishNameChange("Plato")
        viewModel.onRatingChange(5)
        viewModel.onCommentChange("Muy bueno")
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onImagePreparationStarted()
        viewModel.onImagePrepared(ImageUploadValidationResult.Success(testImage))

        assertTrue(viewModel.uiState.value.canSubmit)
        viewModel.onImagePreparationStarted()
        assertFalse(viewModel.uiState.value.canSubmit)
        assertTrue(viewModel.uiState.value.isPreparingImage)
    }

    @Test
    fun inputIsLimitedAndInvalidRatingsAreIgnored() {
        val viewModel = viewModelFor()
        viewModel.onRestaurantNameChange("r".repeat(140))
        viewModel.onDishNameChange("d".repeat(140))
        viewModel.onCommentChange("c".repeat(2200))
        viewModel.onRatingChange(6)

        assertEquals(UnlistedDishReviewUiState.MAX_RESTAURANT_NAME_LENGTH, viewModel.uiState.value.restaurantName.length)
        assertEquals(UnlistedDishReviewUiState.MAX_DISH_NAME_LENGTH, viewModel.uiState.value.dishName.length)
        assertEquals(UnlistedDishReviewUiState.MAX_COMMENT_LENGTH, viewModel.uiState.value.comment.length)
        assertEquals(0, viewModel.uiState.value.rating)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun invalidPickedImageShowsImageErrorAndClearsPreparingState() {
        val viewModel = viewModelFor()
        viewModel.onImagePreparationStarted()
        viewModel.onImagePrepared(ImageUploadValidationResult.TooLarge)

        assertFalse(viewModel.uiState.value.isPreparingImage)
        assertEquals(UnlistedDishReviewImageError.TOO_LARGE, viewModel.uiState.value.imageError)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submitPassesAllFieldsAndDisablesDuplicateSubmission() = runTest(dispatcher) {
        var received: SubmitUnlistedDishReviewParams? = null
        val viewModel = viewModelFor { params ->
            received = params
            RepositoryResult.Success(testReview)
        }
        fillForm(viewModel)

        viewModel.onSubmitClick()
        assertTrue(viewModel.uiState.value.isSubmitting)
        viewModel.onSubmitClick()
        advanceUntilIdle()

        assertEquals(
            SubmitUnlistedDishReviewParams("Restaurante", "Plato", testImage, 4, "Muy bueno"),
            received,
        )
        assertTrue(viewModel.uiState.value.submitSucceeded)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submissionFailureMapsErrorAndKeepsFormAvailableForRetry() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Failure(RepositoryError.Offline) }
        fillForm(viewModel)

        viewModel.onSubmitClick()
        advanceUntilIdle()

        assertEquals(UnlistedDishReviewError.OFFLINE, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun editingAfterFailureClearsTheError() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Failure(RepositoryError.Forbidden) }
        fillForm(viewModel)
        viewModel.onSubmitClick()
        advanceUntilIdle()

        viewModel.onCommentChange("Otra opinión")

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.submitSucceeded)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun reopeningFormResetsPreviousFieldsImageAndSuccessState() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        val firstCompositionToken = Any()
        viewModel.beginNewReview(openingToken = firstCompositionToken)
        fillForm(viewModel)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.submitSucceeded)

        // Recomposition within the same composition session keeps the existing presentation token.
        viewModel.beginNewReview(openingToken = firstCompositionToken)
        assertTrue(viewModel.uiState.value.submitSucceeded)

        // Navigation may destroy and recreate its composition with all remember state reset.
        // A new identity token must still distinguish that opening from the retained ViewModel.
        val recreatedCompositionToken = Any()
        assertFalse(firstCompositionToken === recreatedCompositionToken)
        viewModel.beginNewReview(openingToken = recreatedCompositionToken)

        assertEquals(UnlistedDishReviewUiState(openingToken = recreatedCompositionToken), viewModel.uiState.value)
    }

    @Test
    fun ratingSelectionHelperDescribesOnlyTheChosenOptionAsSelected() {
        val state = UnlistedDishReviewUiState(rating = 4)

        assertFalse(state.isRatingSelected(3))
        assertTrue(state.isRatingSelected(4))
        assertFalse(state.isRatingSelected(5))
        assertFalse(UnlistedDishReviewUiState().isRatingSelected(0))
    }
}

private val testImage = ImageUpload(byteArrayOf(1, 2, 3), "image/jpeg", "plato.jpg")
private val testReview = UnlistedDishReview(
    id = ReviewId("review-1"),
    authorAccountId = AccountId("customer-1"),
    restaurantName = "Restaurante",
    dishName = "Plato",
    image = ImageRef("https://example.com/review.jpg", "Plato"),
    rating = Rating(4),
    comment = "Muy bueno",
    visibility = ReviewVisibility.Public,
    moderationStatus = ReviewModerationStatus.Hidden,
    createdAt = IsoTimestamp("2026-10-03T12:00:00Z"),
    updatedAt = IsoTimestamp("2026-10-03T12:00:00Z"),
)

private fun fillForm(viewModel: UnlistedDishReviewViewModel) {
    viewModel.onRestaurantNameChange("Restaurante")
    viewModel.onDishNameChange("Plato")
    viewModel.onImagePrepared(ImageUploadValidationResult.Success(testImage))
    viewModel.onRatingChange(4)
    viewModel.onCommentChange("Muy bueno")
}

private fun viewModelFor(
    submit: SubmitUnlistedDishReviewUseCase = SubmitUnlistedDishReviewUseCase {
        RepositoryResult.Success(testReview)
    },
) = UnlistedDishReviewViewModel(submit)
