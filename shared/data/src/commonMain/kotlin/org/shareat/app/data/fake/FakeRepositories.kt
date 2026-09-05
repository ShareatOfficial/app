package org.shareat.app.data.fake

import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

/** Convenient shared graph for UI previews and application DI. */
data class FakeRepositories(
    val accounts: AccountRepository,
    val restaurants: RestaurantRepository,
    val menus: MenuRepository,
    val dishes: DishRepository,
    val reviews: ReviewRepository,
) {
    companion object {
        fun create(
            scenario: FakeDataScenario = FakeDataScenario.Populated,
            data: FakeShareatData = when (scenario) {
                FakeDataScenario.Empty -> FakeShareatData.empty()
                else -> FakeShareatData.preview()
            },
        ): FakeRepositories = FakeRepositories(
            accounts = FakeAccountRepository(data, scenario),
            restaurants = FakeRestaurantRepository(data, scenario),
            menus = FakeMenuRepository(data, scenario),
            dishes = FakeDishRepository(data, scenario),
            reviews = FakeReviewRepository(data, scenario),
        )
    }
}
