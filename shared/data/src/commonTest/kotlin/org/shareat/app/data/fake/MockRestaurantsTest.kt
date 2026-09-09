package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.RepositoryResult

class MockRestaurantsTest {

    @Test
    fun mocksFiveRestaurantsByDefault() = runSuspend {
        assertEquals(DefaultMockRestaurantCount, publishedRestaurants(mockShareatData()).size)
    }

    @Test
    fun theRequestedNumberOfRestaurantsIsHonoured() = runSuspend {
        assertEquals(1, publishedRestaurants(mockShareatData(restaurantCount = 1)).size)
        assertEquals(3, publishedRestaurants(mockShareatData(restaurantCount = 3)).size)
        assertEquals(9, publishedRestaurants(mockShareatData(restaurantCount = 50)).size)
    }

    @Test
    fun everyMockedRestaurantIsPublishedAndDistinct() = runSuspend {
        val restaurants = publishedRestaurants(mockShareatData())

        assertTrue(restaurants.all { it.publicationState == RestaurantPublicationState.Published })
        assertEquals(restaurants.size, restaurants.map(Restaurant::name).distinct().size)
        assertEquals(restaurants.size, restaurants.map { it.address.locality }.distinct().size)
    }

    @Test
    fun everyPublishedMenuServesDishesWithPricesAndCategories() = runSuspend {
        val data = mockShareatData()
        val menus = FakeMenuRepository(data)

        publishedRestaurants(data).forEach { restaurant ->
            publishedMenus(menus, restaurant.id).forEach { menu ->
                val details = assertIs<RepositoryResult.Success<MenuDetails>>(
                    menus.getMenu(menu.id),
                ).value
                assertTrue(details.items.isNotEmpty(), "${menu.name} of ${restaurant.name} is empty")
                assertTrue(details.items.all { it.price.minorUnits > 0 })
                assertTrue(details.items.all { it.category != null })
            }
        }
    }

    @Test
    fun theDefaultMockCoversRatedAndUnratedRestaurants() = runSuspend {
        val data = mockShareatData()
        val reviews = FakeReviewRepository(data)
        val summaries = publishedRestaurants(data).map { restaurant ->
            assertIs<RepositoryResult.Success<RatingSummary>>(
                reviews.getRatingSummary(ReviewTarget.Restaurant(restaurant.id)),
            ).value
        }

        assertTrue(summaries.any { it.averageTenths == null }, "expected an unrated restaurant")
        assertTrue(summaries.count { it.averageTenths != null } >= 4)
    }

    @Test
    fun theDefaultMockCoversRestaurantsWithAndWithoutAPublishedMenu() = runSuspend {
        val data = mockShareatData()
        val menus = FakeMenuRepository(data)
        val published = publishedRestaurants(data).map { publishedMenus(menus, it.id) }

        assertTrue(published.any { it.isEmpty() }, "expected a restaurant with no public menu")
        assertTrue(published.count { it.isNotEmpty() } >= 4)
    }

    @Test
    fun dishesCarryReviewsSoRatingsAreNotAllEmpty() = runSuspend {
        val data = mockShareatData()
        val reviews = FakeReviewRepository(data)

        val ratedDishes = data.dishes.count { dish ->
            val summary = assertIs<RepositoryResult.Success<RatingSummary>>(
                reviews.getRatingSummary(ReviewTarget.Dish(dish.id)),
            ).value
            summary.averageTenths != null
        }

        assertTrue(ratedDishes >= 4, "expected several rated dishes, got $ratedDishes")
    }

    @Test
    fun theCatalogueCanBeConsumedWithoutAFakeShareatData() {
        val catalogue = mockCatalogue()

        assertEquals(DefaultMockRestaurantCount - 1, catalogue.restaurants.size)
        assertTrue(catalogue.menus.isNotEmpty())
        assertTrue(catalogue.dishes.isNotEmpty())
        assertTrue(catalogue.menuItems.isNotEmpty())
        assertTrue(catalogue.reviews.isNotEmpty())
        assertTrue(catalogue.accounts.isNotEmpty())
    }

    private suspend fun publishedRestaurants(data: FakeShareatData): List<Restaurant> =
        assertIs<RepositoryResult.Success<List<Restaurant>>>(
            FakeRestaurantRepository(data).getPublishedRestaurants(),
        ).value

    private suspend fun publishedMenus(
        menus: FakeMenuRepository,
        restaurantId: RestaurantId,
    ): List<Menu> = assertIs<RepositoryResult.Success<List<Menu>>>(menus.getMenus(restaurantId))
        .value
        .filter { it.publicationState == MenuPublicationState.Published }
}
