package org.shareat.app.domain.usecase

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetRestaurantUseCaseImplTest {

    @Test
    fun returnsTheRequestedRestaurantWithItsPublishedMenus() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id, name = "Menú del día")
        val dish = dishFixture(id = "dish-1", restaurantId = restaurant.id, name = "Croquetas")
        val useCase = GetRestaurantUseCaseImpl(
            restaurantRepository = FakeRestaurantRepository { RepositoryResult.Success(listOf(restaurant)) },
            assembler = assemblerFor(
                menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
                dishesByMenu = mapOf(menu.id to listOf(menuDishFixture(dish, position = 0))),
            ),
        )

        val result = useCase(restaurant.id)

        val details = assertIs<RepositoryResult.Success<RestaurantDetails>>(result).value
        assertEquals(restaurant.id, details.restaurant.id)
        assertEquals("Menú del día", details.menu?.menu?.name)
        assertEquals(listOf("Croquetas"), details.menu?.dishes.orEmpty().map { it.menuDish.dish.name })
    }

    @Test
    fun aMenuDishCarriesItsPublicReviewsAndDerivesItsSummaryFromThem() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id, name = "Menú del día")
        val dish = dishFixture(id = "dish-1", restaurantId = restaurant.id, name = "Croquetas")
        val useCase = GetRestaurantUseCaseImpl(
            restaurantRepository = FakeRestaurantRepository { RepositoryResult.Success(listOf(restaurant)) },
            assembler = assemblerFor(
                menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
                dishesByMenu = mapOf(menu.id to listOf(menuDishFixture(dish, position = 0))),
                reviewsByDish = mapOf(
                    dish.id to listOf(
                        reviewFixture(comment = "Crujientes.", rating = 5, createdAt = "2026-08-13T12:30:00Z"),
                        reviewFixture(comment = null, rating = 4, createdAt = "2026-08-12T12:30:00Z"),
                    ),
                ),
            ),
        )

        val result = useCase(restaurant.id)

        val ratedDish = assertIs<RepositoryResult.Success<RestaurantDetails>>(result)
            .value.menu?.dishes.orEmpty().single()
        assertEquals(listOf(5, 4), ratedDish.reviews.map { it.rating.value })
        assertEquals(RatingSummary(averageTenths = 45, ratingCount = 2), ratedDish.ratingSummary)
    }

    @Test
    fun aMenuDishWithoutPublicReviewsIsUnrated() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id, name = "Menú del día")
        val dish = dishFixture(id = "dish-1", restaurantId = restaurant.id, name = "Croquetas")
        val useCase = GetRestaurantUseCaseImpl(
            restaurantRepository = FakeRestaurantRepository { RepositoryResult.Success(listOf(restaurant)) },
            assembler = assemblerFor(
                menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
                dishesByMenu = mapOf(menu.id to listOf(menuDishFixture(dish, position = 0))),
            ),
        )

        val result = useCase(restaurant.id)

        val ratedDish = assertIs<RepositoryResult.Success<RestaurantDetails>>(result)
            .value.menu?.dishes.orEmpty().single()
        assertEquals(RatingSummary.Unrated, ratedDish.ratingSummary)
    }

    @Test
    fun dishReviewsAreFetchedInOneBatchPerSectionInsteadOfOncePerDish() = runTest {
        val restaurant = restaurantFixture()
        val menu = menuFixture(id = "menu-1", restaurantId = restaurant.id)
        val dishes = List(4) { index ->
            dishFixture(id = "dish-$index", restaurantId = restaurant.id, name = "Plato $index")
        }
        val reviews = FakeReviewRepository(
            reviewsByDish = dishes.associate { dish ->
                dish.id to listOf(
                    reviewFixture(comment = "Rico.", rating = 5, createdAt = "2026-08-13T12:30:00Z"),
                )
            },
        )
        val useCase = GetRestaurantUseCaseImpl(
            restaurantRepository = FakeRestaurantRepository { RepositoryResult.Success(listOf(restaurant)) },
            assembler = RestaurantDetailsAssembler(
                menuRepository = FakeMenuRepository(
                    menusByRestaurant = mapOf(restaurant.id to listOf(menu)),
                    dishesByMenu = mapOf(
                        menu.id to dishes.mapIndexed { index, dish -> menuDishFixture(dish, index) },
                    ),
                ),
                dishRepository = FakeDishRepository(mapOf(restaurant.id to dishes)),
                reviewRepository = reviews,
            ),
        )

        useCase(restaurant.id)

        assertEquals(2, reviews.dishReviewRequests)
    }

    @Test
    fun propagatesNotFound() = runTest {
        val useCase = GetRestaurantUseCaseImpl(
            restaurantRepository = FakeRestaurantRepository { RepositoryResult.Success(emptyList()) },
            assembler = assemblerFor(),
        )

        val result = useCase(RestaurantId("restaurant-missing"))

        val failure = assertIs<RepositoryResult.Failure>(result)
        assertEquals(RepositoryError.NotFound("Restaurant", "restaurant-missing"), failure.error)
    }
}
