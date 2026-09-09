package org.shareat.app.domain.usecase

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GetRestaurantMenuUseCaseImplTest {

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
        val useCase = useCaseFor(menusByRestaurant = mapOf(restaurant.id to listOf(draft, published)))

        val result = useCase(restaurant.id)

        assertEquals("Carta", assertIs<RepositoryResult.Success<RestaurantMenu?>>(result).value?.menu?.name)
    }

    @Test
    fun aRestaurantWithoutAPublishedMenuSucceedsWithNoMenu() = runTest {
        val restaurant = restaurantFixture()
        val draft = menuFixture(
            id = "menu-draft",
            restaurantId = restaurant.id,
            publicationState = MenuPublicationState.Draft,
        )
        val useCase = useCaseFor(menusByRestaurant = mapOf(restaurant.id to listOf(draft)))

        val result = useCase(restaurant.id)

        assertNull(assertIs<RepositoryResult.Success<RestaurantMenu?>>(result).value)
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

        val result = useCase(restaurant.id)

        val menuResult = assertIs<RepositoryResult.Success<RestaurantMenu?>>(result).value
        assertEquals(listOf("Croquetas", "Pulpo"), menuResult?.dishes.orEmpty().map { it.menuDish.dish.name })
    }

    @Test
    fun aMenuDishCarriesItsPublicReviewsFetchedInOneBatch() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id)
        val dishes = List(3) { index ->
            dishFixture(id = "dish-$index", restaurantId = restaurant.id, name = "Plato $index")
        }
        val reviews = FakeReviewRepository(
            reviewsByDish = dishes.associate { dish ->
                dish.id to listOf(reviewFixture(comment = "Rico.", rating = 5, createdAt = "2026-08-13T12:30:00Z"))
            },
        )
        val useCase = GetRestaurantMenuUseCaseImpl(
            PublishedMenuAssembler(
                menuRepository = FakeMenuRepository(
                    menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
                    dishesByMenu = mapOf(
                        menu.id to dishes.mapIndexed { index, dish -> menuDishFixture(dish, index) },
                    ),
                ),
                reviewRepository = reviews,
            ),
        )

        val result = useCase(restaurant.id)

        val ratedDishes = assertIs<RepositoryResult.Success<RestaurantMenu?>>(result).value?.dishes.orEmpty()
        assertEquals(1, reviews.dishReviewRequests)
        assertEquals(
            List(3) { RatingSummary(averageTenths = 50, ratingCount = 1) },
            ratedDishes.map { it.ratingSummary },
        )
    }

    @Test
    fun propagatesARepositoryFailureInsteadOfReportingNoMenu() = runTest {
        val useCase = GetRestaurantMenuUseCaseImpl(
            PublishedMenuAssembler(
                menuRepository = FailingMenuRepository,
                reviewRepository = FakeReviewRepository(),
            ),
        )

        val result = useCase(RestaurantId("restaurant-1"))

        assertIs<RepositoryResult.Failure>(result)
    }

    private fun useCaseFor(
        menusByRestaurant: Map<RestaurantId, List<Menu>> = emptyMap(),
        dishesByMenu: Map<MenuId, List<MenuDish>> = emptyMap(),
    ): GetRestaurantMenuUseCase = GetRestaurantMenuUseCaseImpl(
        PublishedMenuAssembler(
            menuRepository = FakeMenuRepository(menusByRestaurant, dishesByMenu),
            reviewRepository = FakeReviewRepository(),
        ),
    )
}
