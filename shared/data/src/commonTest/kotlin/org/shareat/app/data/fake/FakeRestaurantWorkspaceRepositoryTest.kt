package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeRestaurantWorkspaceRepositoryTest {
    @Test
    fun createsTheCompleteDraftWorkspaceWithDeterministicStarterContent() = runSuspend {
        val owner = restaurantOwner()
        val data = emptyData(owner)
        val repository = FakeRestaurantWorkspaceRepository(data, FakeDataScenario.Empty)

        val result = repository.ensureRestaurantWorkspace(owner.id)

        val restaurant = assertIs<RepositoryResult.Success<Restaurant>>(result).value
        assertEquals("restaurant-${owner.id.value}", restaurant.id.value)
        assertEquals("Rincón de Paco", restaurant.name)
        assertEquals("Lugar típico para tomarse unas tapas.", restaurant.description)
        assertEquals(RestaurantPublicationState.Draft, restaurant.publicationState)
        assertEquals(null, restaurant.heroImage)
        assertEquals("ES", restaurant.address.countryCode)
        assertEquals(1, data.restaurants.size)
        assertEquals(1, data.menus.size)
        assertEquals(MenuPublicationState.Draft, data.menus.single().publicationState)
        assertEquals(listOf("Patatas bravas", "Croquetas de jamón"), data.dishes.map { it.name })
        assertEquals(listOf(650L, 850L), data.menuItems.map { it.price.minorUnits })
        assertEquals(
            listOf(DishCategory.SmallBites, DishCategory.Starters),
            data.menuItems.map { it.category },
        )
    }

    @Test
    fun retryReturnsExistingRestaurantWithoutReseedingOrOverwritingIt() = runSuspend {
        val owner = restaurantOwner()
        val existing = Restaurant(
            id = RestaurantId("restaurant-existing"),
            ownerAccountId = owner.id,
            name = "Ya personalizado",
            description = "No debe cambiarse.",
            address = PostalAddress("Calle Real, 4", "Madrid", "28001"),
            openingHours = WeeklyOpeningHours(emptyList()),
            publicationState = RestaurantPublicationState.Draft,
        )
        val data = FakeShareatData(
            accounts = listOf(owner),
            customerProfiles = emptyList(),
            restaurants = listOf(existing),
            menus = emptyList(),
            dishes = emptyList(),
            menuItems = emptyList(),
            reviews = emptyList(),
        )
        val repository = FakeRestaurantWorkspaceRepository(data)

        val result = repository.ensureRestaurantWorkspace(owner.id)

        assertEquals(existing, assertIs<RepositoryResult.Success<Restaurant>>(result).value)
        assertEquals(1, data.restaurants.size)
        assertEquals(0, data.menus.size)
        assertEquals(0, data.dishes.size)
        assertEquals(0, data.menuItems.size)
    }

    @Test
    fun rejectsANonRestaurantOrInactiveOwner() = runSuspend {
        val customer = restaurantOwner().copy(role = AccountRole.Customer)
        val data = emptyData(customer)

        val result = FakeRestaurantWorkspaceRepository(data).ensureRestaurantWorkspace(customer.id)

        assertEquals(RepositoryResult.Failure(RepositoryError.Forbidden), result)
        assertEquals(0, data.restaurants.size)
    }

    private fun restaurantOwner() = Account(
        id = AccountId("restaurant-new-owner"),
        loginEmail = EmailAddress("new-owner@example.test"),
        role = AccountRole.Restaurant,
        status = AccountStatus.Active,
    )

    private fun emptyData(account: Account) = FakeShareatData(
        accounts = listOf(account),
        customerProfiles = emptyList(),
        restaurants = emptyList(),
        menus = emptyList(),
        dishes = emptyList(),
        menuItems = emptyList(),
        reviews = emptyList(),
    )
}
