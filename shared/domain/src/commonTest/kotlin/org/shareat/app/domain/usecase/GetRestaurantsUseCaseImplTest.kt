package org.shareat.app.domain.usecase

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetRestaurantsUseCaseImplTest {

    @Test
    fun paginatesTheRealRestaurantsByOffsetAndLimit() = runTest {
        val restaurants = (0 until 5).map { restaurantFixture(id = "restaurant-$it", name = "Restaurant $it") }
        val useCase = useCaseFor(seed = restaurants)

        val result = useCase(page = 2, numberOfRestaurants = 2)

        val page = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value
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

        val result = useCase(page = 0, numberOfRestaurants = 2)

        val page = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value
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

        val result = useCase(page = 0, numberOfRestaurants = 1)

        val card = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value.single()
        assertEquals(3, card.dishHighlights.size)
        assertEquals(
            listOf("Pulpo a la brasa", "Croquetas", "Croquetas"),
            card.dishHighlights.map { it.dish.name },
        )
        assertTrue(card.dishHighlights.none { it.review.rating.value == 1 })
    }

    @Test
    fun exposesOnlyThePublishedMenu() = runTest {
        val restaurant = restaurantFixture()
        val draft = menuFixture(
            id = "menu-draft",
            restaurantId = restaurant.id,
            name = "Menú de temporada",
            publicationState = MenuPublicationState.Draft,
        )
        val published = menuFixture(id = "menu-published", restaurantId = restaurant.id, name = "Carta")

        val useCase = useCaseFor(
            seed = listOf(restaurant),
            menusByRestaurant = mapOf(restaurant.id to listOf(draft, published)),
        )

        val result = useCase(page = 0, numberOfRestaurants = 1)

        val details = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value.single()
        assertEquals("Carta", details.menu?.menu?.name)
    }

    @Test
    fun aRestaurantWithoutAPublishedMenuHasNoMenu() = runTest {
        val restaurant = restaurantFixture()
        val draft = menuFixture(
            id = "menu-draft",
            restaurantId = restaurant.id,
            publicationState = MenuPublicationState.Draft,
        )

        val useCase = useCaseFor(
            seed = listOf(restaurant),
            menusByRestaurant = mapOf(restaurant.id to listOf(draft)),
        )

        val result = useCase(page = 0, numberOfRestaurants = 1)

        val details = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value.single()
        assertNull(details.menu)
    }

    @Test
    fun exposesOnlySellableDishesSortedByPosition() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id)
        val starter = dishFixture(id = "dish-1", restaurantId = restaurant.id, name = "Croquetas")
        val main = dishFixture(id = "dish-2", restaurantId = restaurant.id, name = "Pulpo")
        val archived = dishFixture(id = "dish-3", restaurantId = restaurant.id, name = "Retirado", isEnabled = false)
        val unlisted = dishFixture(id = "dish-4", restaurantId = restaurant.id, name = "Fuera de carta")

        val useCase = useCaseFor(
            seed = listOf(restaurant),
            menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
            dishesByMenu = mapOf(
                menu.id to listOf(
                    menuDishFixture(main, position = 1),
                    menuDishFixture(starter, position = 0),
                    menuDishFixture(archived, position = 2),
                    menuDishFixture(unlisted, position = 3, isEnabled = false),
                ),
            ),
        )

        val result = useCase(page = 0, numberOfRestaurants = 1)

        val details = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value.single()
        assertEquals(
            listOf("Croquetas", "Pulpo"),
            details.menu?.dishes.orEmpty().map { it.menuDish.dish.name },
        )
    }

    @Test
    fun missingRatingSummaryFallsBackToNullAverageAndZeroCount() = runTest {
        val useCase = useCaseFor(
            seed = listOf(restaurantFixture()),
            ratingSummaryResult = { RepositoryResult.Failure(RepositoryError.Unavailable()) },
        )

        val result = useCase(page = 0, numberOfRestaurants = 1)

        val card = assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value.single()
        assertEquals(RatingSummary(averageTenths = null, ratingCount = 0), card.ratingSummary)
    }

    @Test
    fun propagatesRestaurantRepositoryFailure() = runTest {
        val useCase = useCaseFor(restaurantsResult = { RepositoryResult.Failure(RepositoryError.Offline) })

        val result = useCase(page = 0, numberOfRestaurants = 10)

        val failure = assertIs<RepositoryResult.Failure>(result)
        assertEquals(RepositoryError.Offline, failure.error)
    }

    @Test
    fun emptySeedReturnsEmptyPage() = runTest {
        val useCase = useCaseFor(seed = emptyList())

        val result = useCase(page = 0, numberOfRestaurants = 10)

        assertEquals(emptyList(), assertIs<RepositoryResult.Success<List<RestaurantDetails>>>(result).value)
    }

    private fun useCaseFor(
        seed: List<Restaurant> = emptyList(),
        dishesByRestaurant: Map<RestaurantId, List<Dish>> = emptyMap(),
        menusByRestaurant: Map<RestaurantId, List<Menu>> = emptyMap(),
        dishesByMenu: Map<MenuId, List<MenuDish>> = emptyMap(),
        reviewsByDish: Map<DishId, List<Review>> = emptyMap(),
        ratingSummaryResult: (() -> RepositoryResult<RatingSummary>)? = null,
        restaurantsResult: (() -> RepositoryResult<List<Restaurant>>)? = null,
    ): GetRestaurantsUseCase = GetRestaurantsUseCaseImpl(
        restaurantRepository = FakeRestaurantRepository(restaurantsResult ?: { RepositoryResult.Success(seed) }),
        assembler = assemblerFor(
            dishesByRestaurant = dishesByRestaurant,
            menusByRestaurant = menusByRestaurant,
            dishesByMenu = dishesByMenu,
            reviewsByDish = reviewsByDish,
            ratingSummaryResult = ratingSummaryResult,
        ),
    )
}

internal fun assemblerFor(
    dishesByRestaurant: Map<RestaurantId, List<Dish>> = emptyMap(),
    menusByRestaurant: Map<RestaurantId, List<Menu>> = emptyMap(),
    dishesByMenu: Map<MenuId, List<MenuDish>> = emptyMap(),
    reviewsByDish: Map<DishId, List<Review>> = emptyMap(),
    ratingSummaryResult: (() -> RepositoryResult<RatingSummary>)? = null,
): RestaurantDetailsAssembler = RestaurantDetailsAssembler(
    menuRepository = FakeMenuRepository(menusByRestaurant, dishesByMenu),
    dishRepository = FakeDishRepository(dishesByRestaurant),
    reviewRepository = FakeReviewRepository(
        reviewsByDish = reviewsByDish,
        ratingSummaryResult = ratingSummaryResult
            ?: { RepositoryResult.Success(RatingSummary(averageTenths = 48, ratingCount = 2)) },
    ),
)
