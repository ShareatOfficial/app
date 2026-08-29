package org.shareat.feature.home.ui.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.home.domain.DishReviewHighlight
import org.shareat.feature.home.domain.RestaurantWithHighlights
import org.shareat.feature.home.ui.home.model.HomeContentUiState
import org.shareat.feature.home.ui.home.model.HomeFeedSectionUiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun initialStateBeforeLoadCompletesIsLoading() = runTest(dispatcher) {
        val viewModel = viewModelFor { _, _ -> RepositoryResult.Success(listOf(restaurantWithHighlightsFixture())) }

        assertIs<HomeContentUiState.Loading>(viewModel.uiState.value.content)
    }

    @Test
    fun loadsRestaurantsIntoSectionedContent() = runTest(dispatcher) {
        val viewModel = viewModelFor { _, _ ->
            RepositoryResult.Success(
                listOf(restaurantWithHighlightsFixture(name = "Casa Naranja", averageTenths = 48)),
            )
        }

        advanceUntilIdle()

        val content = assertIs<HomeContentUiState.Loaded>(viewModel.uiState.value.content)
        val highlights = assertIs<HomeFeedSectionUiState.Highlights>(content.sections.single())
        val card = highlights.restaurants.single()
        assertEquals("Casa Naranja", card.name)
        assertEquals("4.8", card.ratingLabel)
        assertEquals(1, card.dishReviews.size)
    }

    @Test
    fun filtersRestaurantsByCaseInsensitiveNameAndRebuildsSections() = runTest(dispatcher) {
        val viewModel = viewModelFor { _, _ ->
            RepositoryResult.Success(listOf(restaurantWithHighlightsFixture(name = "Casa Naranja")))
        }
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("naranja")
        val matching = assertIs<HomeContentUiState.Loaded>(viewModel.uiState.value.content)
        assertEquals(1, matching.sections.size)

        viewModel.onSearchQueryChanged("sushi")
        val empty = assertIs<HomeContentUiState.Loaded>(viewModel.uiState.value.content)
        assertTrue(empty.sections.isEmpty())
    }

    @Test
    fun mapsUseCaseFailureToErrorState() = runTest(dispatcher) {
        val viewModel = viewModelFor { _, _ -> RepositoryResult.Failure(RepositoryError.Offline) }

        advanceUntilIdle()

        val content = assertIs<HomeContentUiState.Error>(viewModel.uiState.value.content)
        assertEquals("You appear to be offline. Try again when connected.", content.message)
    }

    @Test
    fun retryReloadsAfterError() = runTest(dispatcher) {
        var shouldFail = true
        val viewModel = viewModelFor { _, _ ->
            if (shouldFail) {
                RepositoryResult.Failure(RepositoryError.Offline)
            } else {
                RepositoryResult.Success(listOf(restaurantWithHighlightsFixture()))
            }
        }
        advanceUntilIdle()
        assertIs<HomeContentUiState.Error>(viewModel.uiState.value.content)

        shouldFail = false
        viewModel.onRetryClick()
        advanceUntilIdle()

        assertIs<HomeContentUiState.Loaded>(viewModel.uiState.value.content)
    }

    private fun viewModelFor(
        result: (page: Int, numberOfRestaurants: Int) -> RepositoryResult<List<RestaurantWithHighlights>>,
    ): HomeViewModel = HomeViewModel(
        getRestaurantsUseCase = { page, numberOfRestaurants -> result(page, numberOfRestaurants) },
    )
}

private fun restaurantWithHighlightsFixture(
    id: String = "restaurant-1",
    name: String = "Casa Naranja",
    averageTenths: Int? = null,
): RestaurantWithHighlights = RestaurantWithHighlights(
    restaurant = Restaurant(
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
    ),
    ratingSummary = RatingSummary(
        averageTenths = averageTenths,
        ratingCount = if (averageTenths == null) 0 else 2,
    ),
    dishHighlights = listOf(
        DishReviewHighlight(
            dish = Dish(
                id = DishId("dish-1"),
                restaurantId = RestaurantId(id),
                name = "Pulpo a la brasa",
                isEnabled = true,
            ),
            review = Review(
                id = ReviewId("review-1"),
                authorAccountId = AccountId("author-1"),
                target = ReviewTarget.Dish(DishId("dish-1")),
                rating = Rating(5),
                comment = "Tiernísimo",
                visibility = ReviewVisibility.Public,
                moderationStatus = ReviewModerationStatus.Visible,
                createdAt = IsoTimestamp("2026-08-10T08:35:00Z"),
                updatedAt = IsoTimestamp("2026-08-10T08:35:00Z"),
            ),
        ),
    ),
    isOpen = true,
)
