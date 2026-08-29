package org.shareat.feature.home.domain

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetHomeRestaurantsUseCaseImplTest {

    @Test
    fun paginatesTheRealRestaurantsByOffsetAndLimit() = runTest {
        val restaurants = (0 until 5).map { restaurantFixture(id = "restaurant-$it", name = "Restaurant $it") }
        val useCase = useCaseFor(seed = restaurants)

        val result = useCase(offset = 2, limit = 2)

        val page = assertIs<RepositoryResult.Success<List<RestaurantWithHighlights>>>(result).value
        assertEquals(listOf("restaurant-2", "restaurant-3"), page.map { it.restaurant.id.value })
    }

    @Test
    fun mapsDishHighlightsPerRestaurantIndependently() = runTest {
        val restaurantOne = restaurantFixture(id = "restaurant-1")
        val restaurantTwo = restaurantFixture(id = "restaurant-2")
        val dishOne = dishFixture(id = "dish-1", restaurantId = restaurantOne.id, name = "Pulpo a la brasa")
        val dishTwo = dishFixture(id = "dish-2", restaurantId = restaurantTwo.id, name = "Croquetas")

        val useCase = useCaseFor(
            seed = listOf(restaurantOne, restaurantTwo),
            dishesByRestaurant = mapOf(
                restaurantOne.id to listOf(dishOne),
                restaurantTwo.id to listOf(dishTwo),
            ),
            reviewsByDish = mapOf(
                dishOne.id to listOf(reviewFixture(comment = "Tiernísimo", rating = 5, createdAt = "2026-08-10T00:00:00Z")),
                dishTwo.id to listOf(reviewFixture(comment = "Cremosas", rating = 4, createdAt = "2026-08-09T00:00:00Z")),
            ),
        )

        val result = useCase(offset = 0, limit = 2)

        val page = assertIs<RepositoryResult.Success<List<RestaurantWithHighlights>>>(result).value
        assertEquals(listOf("Pulpo a la brasa"), page[0].dishHighlights.map { it.dish.name })
        assertEquals(listOf("Croquetas"), page[1].dishHighlights.map { it.dish.name })
    }

    @Test
    fun mapsDishHighlightsCappedAtThreeSortedByRecencyExcludingCommentless() = runTest {
        val restaurant = restaurantFixture()
        val dishOne = dishFixture(id = "dish-1", restaurantId = restaurant.id, name = "Pulpo a la brasa")
        val dishTwo = dishFixture(id = "dish-2", restaurantId = restaurant.id, name = "Croquetas")

        val useCase = useCaseFor(
            seed = listOf(restaurant),
            dishesByRestaurant = mapOf(restaurant.id to listOf(dishOne, dishTwo)),
            reviewsByDish = mapOf(
                dishOne.id to listOf(
                    reviewFixture(comment = "Tiernísimo", rating = 5, createdAt = "2026-08-10T08:35:00Z"),
                    reviewFixture(comment = null, rating = 3, createdAt = "2026-08-11T00:00:00Z"),
                    reviewFixture(comment = "El más flojo", rating = 1, createdAt = "2026-08-06T00:00:00Z"),
                ),
                dishTwo.id to listOf(
                    reviewFixture(comment = "Cremosas", rating = 4, createdAt = "2026-08-09T08:35:00Z"),
                    reviewFixture(comment = "Correctas", rating = 2, createdAt = "2026-08-08T00:00:00Z"),
                ),
            ),
        )

        val result = useCase(offset = 0, limit = 1)

        val card = assertIs<RepositoryResult.Success<List<RestaurantWithHighlights>>>(result).value.single()
        assertEquals(3, card.dishHighlights.size)
        assertEquals(
            listOf("Pulpo a la brasa", "Croquetas", "Croquetas"),
            card.dishHighlights.map { it.dish.name },
        )
        assertTrue(card.dishHighlights.none { it.review.rating.value == 1 })
    }

    @Test
    fun missingRatingSummaryFallsBackToNullAverageAndZeroCount() = runTest {
        val useCase = useCaseFor(
            seed = listOf(restaurantFixture()),
            ratingSummaryResult = { RepositoryResult.Failure(RepositoryError.Unavailable()) },
        )

        val result = useCase(offset = 0, limit = 1)

        val card = assertIs<RepositoryResult.Success<List<RestaurantWithHighlights>>>(result).value.single()
        assertEquals(RatingSummary(averageTenths = null, ratingCount = 0), card.ratingSummary)
    }

    @Test
    fun propagatesRestaurantRepositoryFailure() = runTest {
        val useCase = useCaseFor(restaurantsResult = { RepositoryResult.Failure(RepositoryError.Offline) })

        val result = useCase(offset = 0, limit = 10)

        val failure = assertIs<RepositoryResult.Failure>(result)
        assertEquals(RepositoryError.Offline, failure.error)
    }

    @Test
    fun emptySeedReturnsEmptyPage() = runTest {
        val useCase = useCaseFor(seed = emptyList())

        val result = useCase(offset = 0, limit = 10)

        assertEquals(emptyList(), assertIs<RepositoryResult.Success<List<RestaurantWithHighlights>>>(result).value)
    }

    private fun useCaseFor(
        seed: List<Restaurant> = emptyList(),
        dishesByRestaurant: Map<RestaurantId, List<Dish>> = emptyMap(),
        reviewsByDish: Map<DishId, List<Review>> = emptyMap(),
        ratingSummaryResult: (() -> RepositoryResult<RatingSummary>)? = null,
        restaurantsResult: (() -> RepositoryResult<List<Restaurant>>)? = null,
    ): GetHomeRestaurantsUseCase = GetHomeRestaurantsUseCaseImpl(
        restaurantRepository = FakeRestaurantRepository(restaurantsResult ?: { RepositoryResult.Success(seed) }),
        dishRepository = FakeDishRepository(dishesByRestaurant),
        reviewRepository = FakeReviewRepository(
            reviewsByDish = reviewsByDish,
            ratingSummaryResult = ratingSummaryResult
                ?: { RepositoryResult.Success(RatingSummary(averageTenths = 48, ratingCount = 2)) },
        ),
    )
}

private fun restaurantFixture(
    id: String = "restaurant-1",
    name: String = "Casa Naranja",
): Restaurant = Restaurant(
    id = RestaurantId(id),
    ownerAccountId = AccountId("owner-1"),
    name = name,
    heroImage = ImageRef(url = "https://images.example.com/restaurants/$id.jpg"),
    address = PostalAddress(
        streetLine = "Calle del Olmo, 18",
        locality = "Madrid",
        postalCode = "28015",
    ),
    openingHours = WeeklyOpeningHours(emptyList()),
    publicationState = RestaurantPublicationState.Published,
)

private fun dishFixture(
    id: String,
    restaurantId: RestaurantId,
    name: String,
): Dish = Dish(
    id = DishId(id),
    restaurantId = restaurantId,
    name = name,
    isEnabled = true,
)

private var reviewSequence = 0

private fun reviewFixture(
    comment: String?,
    rating: Int,
    createdAt: String,
): Review = Review(
    id = ReviewId("review-${reviewSequence++}"),
    authorAccountId = AccountId("author-1"),
    target = ReviewTarget.Dish(DishId("unused")),
    rating = Rating(rating),
    comment = comment,
    visibility = ReviewVisibility.Public,
    moderationStatus = ReviewModerationStatus.Visible,
    createdAt = IsoTimestamp(createdAt),
    updatedAt = IsoTimestamp(createdAt),
)

private class FakeRestaurantRepository(
    private val restaurants: () -> RepositoryResult<List<Restaurant>>,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>> = restaurants()
    override suspend fun getRestaurant(id: RestaurantId) = unavailable<Restaurant>()
    override suspend fun getRestaurantForOwner(accountId: AccountId) = unavailable<Restaurant>()
    override suspend fun updateRestaurant(restaurant: Restaurant) = unavailable<Restaurant>()
}

private class FakeDishRepository(
    private val dishesByRestaurant: Map<RestaurantId, List<Dish>>,
) : DishRepository {
    override suspend fun getDish(id: DishId) = unavailable<Dish>()
    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> =
        RepositoryResult.Success(dishesByRestaurant[restaurantId].orEmpty())
}

private class FakeReviewRepository(
    private val reviewsByDish: Map<DishId, List<Review>>,
    private val ratingSummaryResult: () -> RepositoryResult<RatingSummary>,
) : ReviewRepository {
    override suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>> =
        when (target) {
            is ReviewTarget.Dish -> RepositoryResult.Success(reviewsByDish[target.dishId].orEmpty())
            is ReviewTarget.Restaurant -> RepositoryResult.Success(emptyList())
        }

    override suspend fun getReviewsByAuthor(accountId: AccountId) = unavailable<List<Review>>()
    override suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary> =
        ratingSummaryResult()

    override suspend fun saveReview(draft: ReviewDraft) = unavailable<Review>()
    override suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId) = unavailable<Unit>()
}

private fun <T> unavailable(): RepositoryResult<T> = RepositoryResult.Failure(RepositoryError.Unavailable())
