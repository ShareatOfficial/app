package org.shareat.app.data.fake

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeDishRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
) : DishRepository {
    override suspend fun getDish(id: DishId): RepositoryResult<Dish> = scenario.result(
        populated = {
            data.dishes.firstOrNull { it.id == id }
                ?: return RepositoryResult.Failure(RepositoryError.NotFound("Dish", id.value))
        },
        empty = {
            return RepositoryResult.Failure(RepositoryError.NotFound("Dish", id.value))
        },
    )

    override suspend fun getDishes(restaurantId: RestaurantId) = scenario.result(
        populated = { data.dishes.filter { it.restaurantId == restaurantId } },
        empty = { emptyList() },
    )

}
