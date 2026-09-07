package org.shareat.feature.review.domain

import kotlinx.coroutines.test.runTest
import org.shareat.app.data.fake.FakeAccountRepository
import org.shareat.app.data.fake.FakeAuthRepository
import org.shareat.app.data.fake.FakeDishRepository
import org.shareat.app.data.fake.FakeIds
import org.shareat.app.data.fake.FakeReviewRepository
import org.shareat.app.data.fake.FakeShareatData
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SubmitDishReviewsUseCaseTest {
    @Test
    fun savesDishAndRestaurantReviewsForTheAuthenticatedCustomer() = runTest {
        val data = FakeShareatData.preview()
        val reviewRepository = FakeReviewRepository(data)
        val useCase = useCaseFor(data, reviewRepository, authenticated = true)

        val result = useCase(
            SubmitDishReviewsParams(
                dishId = FakeIds.octopus,
                dishRating = 4,
                dishComment = "  Crujiente y sabroso  ",
                restaurantRating = 3,
                restaurantComment = "   ",
            ),
        )

        assertIs<RepositoryResult.Success<Unit>>(result)
        val reviews = assertIs<RepositoryResult.Success<*>>(
            reviewRepository.getReviewsByAuthor(FakeIds.customerAccount),
        ).value as List<*>
        val dishReview = reviews.filterIsInstance<org.shareat.app.domain.model.Review>()
            .single { it.target == ReviewTarget.Dish(FakeIds.octopus) }
        val restaurantReview = reviews.filterIsInstance<org.shareat.app.domain.model.Review>()
            .single { it.target == ReviewTarget.Restaurant(FakeIds.restaurant) }

        assertEquals(4, dishReview.rating.value)
        assertEquals("Crujiente y sabroso", dishReview.comment)
        assertEquals(3, restaurantReview.rating.value)
        assertNull(restaurantReview.comment)
    }

    @Test
    fun rejectsAnUnauthenticatedSubmission() = runTest {
        val data = FakeShareatData.preview()
        val result = useCaseFor(
            data = data,
            reviewRepository = FakeReviewRepository(data),
            authenticated = false,
        )(
            SubmitDishReviewsParams(
                dishId = FakeIds.octopus,
                dishRating = 4,
                dishComment = null,
                restaurantRating = 5,
                restaurantComment = null,
            ),
        )

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Unauthenticated),
            result,
        )
    }

    @Test
    fun rejectsRatingsOutsideTheDomainRange() = runTest {
        val data = FakeShareatData.preview()
        val result = useCaseFor(
            data = data,
            reviewRepository = FakeReviewRepository(data),
            authenticated = true,
        )(
            SubmitDishReviewsParams(
                dishId = FakeIds.octopus,
                dishRating = 0,
                dishComment = null,
                restaurantRating = 5,
                restaurantComment = null,
            ),
        )

        assertIs<RepositoryResult.Failure>(result)
        assertIs<RepositoryError.Validation>(result.error)
    }
}

private fun useCaseFor(
    data: FakeShareatData,
    reviewRepository: FakeReviewRepository,
    authenticated: Boolean,
): SubmitDishReviewsUseCase = SubmitDishReviewsUseCaseImpl(
    authRepository = FakeAuthRepository(initiallyAuthenticated = authenticated),
    accountRepository = FakeAccountRepository(data),
    dishRepository = FakeDishRepository(data),
    reviewRepository = reviewRepository,
)
