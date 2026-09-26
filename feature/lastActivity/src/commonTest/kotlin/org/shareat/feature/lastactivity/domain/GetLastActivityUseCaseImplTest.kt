package org.shareat.feature.lastactivity.domain

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetLastActivityUseCaseImplTest {
    @Test fun sortsReviewsAndEnrichesDishes() = runTest {
        val newer = review("new", "2026-02-02T12:00:00Z")
        val older = review("old", "2026-01-01T12:00:00Z")
        val useCase = GetLastActivityUseCaseImpl(
            reviews(listOf(older, newer)), dishes, unavailableRestaurants,
        )

        val result = assertIs<RepositoryResult.Success<List<LastActivityItem>>>(useCase(AccountId("author")))

        assertEquals(listOf(newer.id, older.id), result.value.map { it.review.id })
        assertEquals("Tortilla", assertIs<ReviewedTarget.Dish>(result.value.first().target).dish.name)
    }

    @Test fun propagatesTargetLookupFailures() = runTest {
        val useCase = GetLastActivityUseCaseImpl(reviews(listOf(review("one", "2026-01-01T12:00:00Z"))), failingDishes, unavailableRestaurants)
        assertIs<RepositoryResult.Failure>(useCase(AccountId("author")))
    }

    @Test fun returnsEmptyAndPropagatesReviewFailures() = runTest {
        assertEquals(emptyList(), assertIs<RepositoryResult.Success<List<LastActivityItem>>>(GetLastActivityUseCaseImpl(reviews(emptyList()), dishes, unavailableRestaurants)(AccountId("author"))).value)
        val failing = object : ReviewRepository by unavailableReviews { override suspend fun getReviewsByAuthor(accountId: AccountId) = RepositoryResult.Failure(RepositoryError.Offline) }
        assertIs<RepositoryResult.Failure>(GetLastActivityUseCaseImpl(failing, dishes, unavailableRestaurants)(AccountId("author")))
    }

    @Test fun enrichesRestaurantTargets() = runTest {
        val review = review("restaurant", "2026-01-01T12:00:00Z").copy(target = ReviewTarget.Restaurant(org.shareat.app.domain.model.RestaurantId("restaurant")))
        val restaurants = object : RestaurantRepository by unavailableRestaurants { override suspend fun getRestaurant(id: org.shareat.app.domain.model.RestaurantId) = RepositoryResult.Success(restaurant()) }
        val result = assertIs<RepositoryResult.Success<List<LastActivityItem>>>(GetLastActivityUseCaseImpl(reviews(listOf(review)), dishes, restaurants)(AccountId("author")))
        assertEquals("Casa", assertIs<ReviewedTarget.Restaurant>(result.value.single().target).restaurant.name)
    }
}

private fun review(id: String, updated: String) = Review(ReviewId(id), AccountId("author"), ReviewTarget.Dish(DishId("dish")), Rating(5), "Excelente", ReviewVisibility.Public, ReviewModerationStatus.Visible, createdAt = IsoTimestamp(updated), updatedAt = IsoTimestamp(updated))
private fun reviews(value: List<Review>) = object : ReviewRepository by unavailableReviews { override suspend fun getReviewsByAuthor(accountId: AccountId) = RepositoryResult.Success(value) }
private val unavailableRestaurants = object : RestaurantRepository { override suspend fun getPublishedRestaurants() = failure<List<org.shareat.app.domain.model.Restaurant>>(); override suspend fun getRestaurant(id: org.shareat.app.domain.model.RestaurantId) = failure<org.shareat.app.domain.model.Restaurant>(); override suspend fun getRestaurantForOwner(accountId: AccountId) = failure<org.shareat.app.domain.model.Restaurant>(); override suspend fun createRestaurantProfile(ownerAccountId: AccountId, draft: org.shareat.app.domain.model.RestaurantProfileDraft) = failure<org.shareat.app.domain.model.Restaurant>(); override suspend fun updateRestaurant(restaurant: org.shareat.app.domain.model.Restaurant) = failure<org.shareat.app.domain.model.Restaurant>() }
private val unavailableDishes = object : DishRepository { override suspend fun getDish(id: DishId) = failure<Dish>(); override suspend fun getDishes(restaurantId: org.shareat.app.domain.model.RestaurantId) = failure<List<Dish>>(); override suspend fun getDishesByRestaurant(restaurantIds: Set<org.shareat.app.domain.model.RestaurantId>) = failure<Map<org.shareat.app.domain.model.RestaurantId, List<Dish>>>(); override suspend fun saveDish(draft: org.shareat.app.domain.model.DishDraft) = failure<Dish>(); override suspend fun archiveDish(id: DishId) = failure<Unit>(); override suspend fun deleteDish(id: DishId) = failure<Unit>() }
private val dishes = object : DishRepository by unavailableDishes { override suspend fun getDish(id: DishId) = RepositoryResult.Success(Dish(id, org.shareat.app.domain.model.RestaurantId("restaurant"), "Tortilla", isEnabled = true)) }
private val failingDishes = object : DishRepository by unavailableDishes { override suspend fun getDish(id: DishId) = RepositoryResult.Failure(RepositoryError.Offline) }
private val unavailableReviews = object : ReviewRepository { override suspend fun getPublicReviews(target: ReviewTarget) = failure<List<Review>>(); override suspend fun getPublicDishReviews(dishIds: Set<DishId>) = failure<Map<DishId, List<Review>>>(); override suspend fun getReviewsByAuthor(accountId: AccountId) = failure<List<Review>>(); override suspend fun getRatingSummary(target: ReviewTarget) = failure<org.shareat.app.domain.model.RatingSummary>(); override suspend fun getRestaurantRatingSummaries(restaurantIds: Set<org.shareat.app.domain.model.RestaurantId>) = failure<Map<org.shareat.app.domain.model.RestaurantId, org.shareat.app.domain.model.RatingSummary>>(); override suspend fun saveReview(draft: org.shareat.app.domain.model.ReviewDraft) = failure<Review>(); override suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId) = failure<Unit>(); override suspend fun reportReview(id: ReviewId, reason: org.shareat.app.domain.model.ReviewReportReason) = failure<Unit>(); override suspend fun blockReviewAuthor(id: ReviewId) = failure<AccountId>() }
private fun <T> failure(): RepositoryResult<T> = RepositoryResult.Failure(RepositoryError.Unavailable())
private fun restaurant() = org.shareat.app.domain.model.Restaurant(org.shareat.app.domain.model.RestaurantId("restaurant"), AccountId("owner"), "Casa", address = org.shareat.app.domain.model.PostalAddress("Calle 1", "Madrid", "28001"), openingHours = org.shareat.app.domain.model.WeeklyOpeningHours(emptyList()), publicationState = org.shareat.app.domain.model.RestaurantPublicationState.Published)
