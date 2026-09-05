package org.shareat.app.data.fake

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
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

    override suspend fun saveDish(draft: DishDraft): RepositoryResult<Dish> = scenario.result(
        populated = { save(draft) },
        empty = { save(draft) },
    )

    override suspend fun archiveDish(id: DishId): RepositoryResult<Unit> = scenario.result(
        populated = { archive(id) },
        empty = { archive(id) },
    )

    override suspend fun deleteDish(id: DishId): RepositoryResult<Unit> = scenario.result(
        populated = { delete(id) },
        empty = { delete(id) },
    )

    private fun save(draft: DishDraft): Dish {
        val id = draft.id ?: DishId("dish-${data.dishes.size + 1}")
        val existing = data.dishes.firstOrNull { it.id == id }
        val dish = Dish(id, draft.restaurantId, draft.name.trim(), draft.description?.trim()?.ifEmpty { null }, existing?.image, draft.allergenDeclaration, draft.isEnabled)
        if (existing == null) data.dishes += dish else data.dishes[data.dishes.indexOf(existing)] = dish
        return dish
    }

    private fun archive(id: DishId) {
        val index = data.dishes.indexOfFirst { it.id == id }
        if (index >= 0) data.dishes[index] = data.dishes[index].copy(isEnabled = false)
        data.menuItems.removeAll { it.dishId == id }
    }

    private fun delete(id: DishId) {
        data.dishes.removeAll { it.id == id }
        data.menuItems.removeAll { it.dishId == id }
    }
}
