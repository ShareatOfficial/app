package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeRestaurantRepositoryTest {
    @Test
    fun ownerAccountResolvesItsSingleRestaurant() = runSuspend {
        val repository = FakeRestaurantRepository(FakeShareatData.preview())

        val result = repository.getRestaurantForOwner(FakeIds.restaurantAccount)

        val restaurant = assertIs<RepositoryResult.Success<*>>(result).value
        assertEquals(FakeIds.restaurant, assertIs<org.shareat.app.domain.model.Restaurant>(restaurant).id)
    }

    @Test
    fun offlineScenarioReturnsTypedFailure() = runSuspend {
        val repository = FakeRestaurantRepository(
            data = FakeShareatData.preview(),
            scenario = FakeDataScenario.Offline,
        )

        val result = repository.getPublishedRestaurants()

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Offline),
            result,
        )
    }
}
