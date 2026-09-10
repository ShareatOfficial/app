package org.shareat.app.domain.repository

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RestaurantId

interface DishRepository {
    suspend fun getDish(id: DishId): RepositoryResult<Dish>
    suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>>

    suspend fun getDishesByRestaurant(
        restaurantIds: Set<RestaurantId>,
    ): RepositoryResult<Map<RestaurantId, List<Dish>>>
    suspend fun saveDish(draft: DishDraft): RepositoryResult<Dish>
    suspend fun archiveDish(id: DishId): RepositoryResult<Unit>
    /** Deletes a dish only when it has no reviews; otherwise returns [RepositoryError.Conflict]. */
    suspend fun deleteDish(id: DishId): RepositoryResult<Unit>
}
