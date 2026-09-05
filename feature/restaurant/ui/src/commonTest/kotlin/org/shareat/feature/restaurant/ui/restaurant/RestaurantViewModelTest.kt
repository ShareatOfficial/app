package org.shareat.feature.restaurant.ui.restaurant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.Rating
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
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.usecase.GetRestaurantUseCase
import org.shareat.app.domain.usecase.RatedMenuDish
import org.shareat.app.domain.usecase.RestaurantDetails
import org.shareat.app.domain.usecase.RestaurantMenu
import org.shareat.feature.restaurant.domain.DishMatchesFiltersUseCaseImpl
import org.shareat.feature.restaurant.ui.model.DishArgs
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelTest {
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
    fun theArgumentsRenderWithoutQueryingTheRepository() = runTest(dispatcher) {
        val viewModel = viewModelFor { fail("The screen must not load anything when it opens") }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Casa Naranja", state.header.name)
        assertEquals(listOf("Croquetas", "Lubina", "Torrija"), state.dishes.map { it.name })
        assertTrue(state.hasPublishedMenu)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun aRestaurantWithoutAPublishedMenuShowsNoDishesAndNoFilters() = runTest(dispatcher) {
        val viewModel = RestaurantViewModel(
            args = argsFixture.copy(dishes = emptyList()),
            getRestaurant = GetRestaurantUseCase { fail("Unexpected repository call") },
            dishMatchesFilters = DishMatchesFiltersUseCaseImpl(),
        )

        val state = viewModel.uiState.value
        assertFalse(state.hasPublishedMenu)
        assertTrue(state.dishes.isEmpty())
        assertTrue(state.categories.isEmpty())
        assertTrue(state.allergenFilter.allergens.isEmpty())
    }

    @Test
    fun theCategoryChipNarrowsTheDishList() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        viewModel.onCategoryClick(DishCategory.Desserts)

        assertEquals(listOf("Torrija"), viewModel.uiState.value.dishes.map { it.name })
    }

    @Test
    fun onlyTheAllergensDeclaredByTheExpandedMenuAreOffered() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        assertEquals(
            listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Fish, EuAllergen.Milk),
            viewModel.uiState.value.allergenFilter.allergens.map { it.allergen },
        )
    }

    @Test
    fun everyAllergenStartsIncludedAndOnlyTheTappedOneIsExcluded() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        assertTrue(viewModel.uiState.value.allergenFilter.allergens.none { it.isExcluded })

        viewModel.onAllergenClick(EuAllergen.Milk)

        val allergens = viewModel.uiState.value.allergenFilter.allergens
        assertEquals(listOf(EuAllergen.Milk), allergens.filter { it.isExcluded }.map { it.allergen })
        assertTrue(allergens.filterNot { it.isExcluded }.isNotEmpty())
    }

    @Test
    fun theAllergenFilterHidesTheDishesDeclaringIt() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        viewModel.onAllergenClick(EuAllergen.Milk)

        val state = viewModel.uiState.value
        assertEquals(listOf("Lubina"), state.dishes.map { it.name })
        assertTrue(state.allergenFilter.hasExclusions)
    }

    @Test
    fun tappingAnAllergenTwiceRestoresTheDishes() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        viewModel.onAllergenClick(EuAllergen.Milk)
        viewModel.onAllergenClick(EuAllergen.Milk)

        assertEquals(3, viewModel.uiState.value.dishes.size)
    }

    @Test
    fun tappingADishExpandsOnlyThatCard() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        viewModel.onDishClick("dish-croquettes")

        val dishes = viewModel.uiState.value.dishes
        assertTrue(dishes.single { it.id == "dish-croquettes" }.isExpanded)
        assertTrue(dishes.filterNot { it.id == "dish-croquettes" }.none { it.isExpanded })
    }

    @Test
    fun pullToRefreshReplacesTheContentWithTheDomainResult() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Success(refreshedDetails()) }

        viewModel.onRefresh()
        assertTrue(viewModel.uiState.value.isRefreshing)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Casa Naranja renovada", state.header.name)
        assertEquals(listOf("Sopa de cebolla"), state.dishes.map { it.name })
    }

    @Test
    fun aFailedRefreshKeepsTheContentAndSurfacesTheError() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Failure(RepositoryError.Offline) }

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(listOf("Croquetas", "Lubina", "Torrija"), state.dishes.map { it.name })
        assertEquals(
            "Parece que no tienes conexión. Inténtalo de nuevo más tarde.",
            state.errorMessage,
        )
    }

    @Test
    fun theErrorIsClearedOnceShown() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Failure(RepositoryError.Offline) }
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.onErrorShown()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun tappingAStarRemembersTheRatingForThatDishOnly() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        viewModel.onDishRatingClick("dish-croquettes", 4)

        val dishes = viewModel.uiState.value.dishes
        assertEquals(4, dishes.single { it.id == "dish-croquettes" }.selectedRating)
        assertTrue(dishes.filterNot { it.id == "dish-croquettes" }.all { it.selectedRating == null })
    }

    @Test
    fun aRememberedRatingIsDroppedWhenTheDishLeavesTheRefreshedMenu() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Success(refreshedDetails()) }
        viewModel.onDishRatingClick("dish-croquettes", 4)

        viewModel.onRefresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.dishes.all { it.selectedRating == null })
    }

    @Test
    fun theRatingAndReviewCountComeFromTheDishReviewList() = runTest(dispatcher) {
        val viewModel = viewModelFor { RepositoryResult.Success(refreshedDetails()) }

        viewModel.onRefresh()
        advanceUntilIdle()

        val dish = viewModel.uiState.value.dishes.single()
        assertEquals("4,5", dish.ratingLabel)
        assertEquals(2, dish.reviewCount)
        assertEquals(listOf("Reconfortante."), dish.comments.map { it.comment })
    }

    @Test
    fun aDishWithoutReviewsHasNoRatingLabel() = runTest(dispatcher) {
        val viewModel = viewModelFor()

        val dish = viewModel.uiState.value.dishes.first()
        assertNull(dish.ratingLabel)
        assertEquals(0, dish.reviewCount)
    }

    private fun viewModelFor(
        getRestaurant: suspend (RestaurantId) -> RepositoryResult<RestaurantDetails> = {
            fail("Unexpected repository call")
        },
    ): RestaurantViewModel = RestaurantViewModel(
        args = argsFixture,
        getRestaurant = GetRestaurantUseCase { id -> getRestaurant(id) },
        dishMatchesFilters = DishMatchesFiltersUseCaseImpl(),
    )
}

private val argsFixture = RestaurantArgs(
    id = "restaurant-casa-naranja",
    name = "Casa Naranja",
    address = "Calle del Olmo, 18, Madrid",
    isOpen = true,
    ratingLabel = "4,8",
    reviewCount = 1_284,
    dishes = listOf(
        DishArgs(
            id = "dish-croquettes",
            name = "Croquetas",
            priceLabel = "12€",
            category = DishCategory.Starters,
            allergens = listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk),
            declaresAllergens = true,
        ),
        DishArgs(
            id = "dish-sea-bass",
            name = "Lubina",
            priceLabel = "24€",
            category = DishCategory.MainCourses,
            allergens = listOf(EuAllergen.Fish),
            declaresAllergens = true,
        ),
        DishArgs(
            id = "dish-french-toast",
            name = "Torrija",
            priceLabel = "6€",
            category = DishCategory.Desserts,
            allergens = listOf(EuAllergen.Milk),
            declaresAllergens = true,
        ),
    ),
)

private fun refreshedDetails(): RestaurantDetails {
    val restaurantId = RestaurantId("restaurant-casa-naranja")
    val menu = Menu(
        id = MenuId("menu-winter"),
        restaurantId = restaurantId,
        name = "Carta de invierno",
        publicationState = MenuPublicationState.Published,
    )
    return RestaurantDetails(
        restaurant = Restaurant(
            id = restaurantId,
            ownerAccountId = AccountId("owner-1"),
            name = "Casa Naranja renovada",
            address = PostalAddress(
                streetLine = "Calle del Olmo, 18",
                locality = "Madrid",
                postalCode = "28015",
            ),
            openingHours = WeeklyOpeningHours(emptyList()),
            publicationState = RestaurantPublicationState.Published,
        ),
        ratingSummary = RatingSummary(averageTenths = 47, ratingCount = 1_300),
        dishHighlights = emptyList(),
        menu = RestaurantMenu(
            menu = menu,
            dishes = listOf(
                RatedMenuDish(
                    menuDish = MenuDish(
                        dish = Dish(
                            id = DishId("dish-onion-soup"),
                            restaurantId = restaurantId,
                            name = "Sopa de cebolla",
                            isEnabled = true,
                        ),
                        price = Money(1_150),
                        position = 0,
                        category = DishCategory.Starters,
                    ),
                    reviews = listOf(
                        dishReview("review-1", rating = 5, comment = "Reconfortante."),
                        dishReview("review-2", rating = 4),
                    ),
                ),
            ),
        ),
        isOpen = true,
    )
}

private fun dishReview(id: String, rating: Int, comment: String? = null): Review = Review(
    id = ReviewId(id),
    authorAccountId = AccountId("customer-1"),
    target = ReviewTarget.Dish(DishId("dish-onion-soup")),
    rating = Rating(rating),
    comment = comment,
    visibility = ReviewVisibility.Public,
    moderationStatus = ReviewModerationStatus.Visible,
    createdAt = IsoTimestamp("2026-08-13T12:30:00Z"),
    updatedAt = IsoTimestamp("2026-08-13T12:30:00Z"),
)
